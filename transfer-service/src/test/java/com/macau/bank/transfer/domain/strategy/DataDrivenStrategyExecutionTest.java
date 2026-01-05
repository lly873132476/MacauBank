package com.macau.bank.transfer.domain.strategy;

import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.transfer.application.result.TransferResult;
import com.macau.bank.transfer.domain.ability.TransferContextBuilder;
import com.macau.bank.transfer.domain.ability.TransferValidator;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.model.AccountSnapshot;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.impl.*;
import com.macau.bank.transfer.domain.service.TransferOrderDomainService;
import com.macau.bank.transfer.domain.statemachine.StateMachineExecutor;
import com.macau.bank.transfer.domain.strategy.impl.CrossBorderTransferStrategy;
import com.macau.bank.transfer.domain.strategy.impl.InternalTransferStrategy;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 全链路策略执行测试 (Real Handlers)
 * <p>
 * 目标：验证 TransferStrategy -> StateMachine -> Real Handlers -> RPC Gateway 的完整链路
 * <p>
 * 特点：
 * 1. 使用 **真实** 的 Handler 实现类 (FreezeFundHandler, DeductFeeHandler 等)
 * 2. 仅 Mock 最底层的 RPC 接口 (AccountGateway, RocketMQTemplate)
 * 3. 验证业务逻辑是否正确调用了底层 RPC
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("全链路真实 Handler 执行测试")
class DataDrivenStrategyExecutionTest {

    // === 核心组件（真实实例） ===
    private StateMachineExecutor stateMachineExecutor;
    private InternalTransferStrategy internalStrategy;
    private CrossBorderTransferStrategy crossBorderStrategy;

    // === 外部 RPC/DB 依赖（Mock） ===
    @Mock
    private TransferContextBuilder contextBuilder;
    @Mock
    private TransferValidator validator;
    @Mock
    private TransferOrderDomainService orderDomainService;
    @Mock
    private TransactionTemplate transactionTemplate;

    // === 核心 RPC 依赖 (被 Handler 调用) ===
    @Mock
    private AccountGateway accountGateway; // 账户服务
    @Mock
    private RocketMQTemplate rocketMQTemplate; // MQ

    @BeforeEach
    void setUp() {
        // 1. 初始化状态机执行器
        stateMachineExecutor = new StateMachineExecutor();
        ReflectionTestUtils.setField(stateMachineExecutor, "transactionTemplate", transactionTemplate);
        ReflectionTestUtils.setField(stateMachineExecutor, "orderDomainService", orderDomainService);

        // Mock 事务执行
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });

        // 2. === 手动装配真实 Handler 链 ===
        List<TransferHandler> handlers = new ArrayList<>();

        // 2.1 FreezeFundBuilder
        FreezeFundHandler freeze = new FreezeFundHandler();
        ReflectionTestUtils.setField(freeze, "accountGateway", accountGateway);
        handlers.add(freeze);

        // 2.2 SendRiskMqHandler
        SendRiskMqHandler sendRisk = new SendRiskMqHandler();
        ReflectionTestUtils.setField(sendRisk, "rocketMQTemplate", rocketMQTemplate);
        handlers.add(sendRisk);

        // 2.3 DeductFeeHandler
        DeductFeeHandler deductFee = new DeductFeeHandler();
        ReflectionTestUtils.setField(deductFee, "accountGateway", accountGateway);
        handlers.add(deductFee);

        // 2.4 DeductMoneyHandler (Confirm Payer)
        DeductMoneyHandler deductPayer = new DeductMoneyHandler();
        ReflectionTestUtils.setField(deductPayer, "accountGateway", accountGateway);
        handlers.add(deductPayer);

        // 2.5 CreditPayeeHandler
        CreditPayeeHandler creditPayee = new CreditPayeeHandler();
        ReflectionTestUtils.setField(creditPayee, "accountGateway", accountGateway);
        handlers.add(creditPayee);

        // 2.6 NotifySwiftHandler (无依赖)
        NotifySwiftHandler notifySwift = new NotifySwiftHandler();
        handlers.add(notifySwift);

        // 2.7 UnfreezeHandler
        UnfreezeHandler unfreeze = new UnfreezeHandler();
        ReflectionTestUtils.setField(unfreeze, "accountGateway", accountGateway);
        handlers.add(unfreeze);

        // 注入到状态机
        stateMachineExecutor.setHandlers(handlers);

        // 3. 初始化并注入 Strategies
        internalStrategy = new InternalTransferStrategy();
        injectDependencies(internalStrategy);

        crossBorderStrategy = new CrossBorderTransferStrategy();
        injectDependencies(crossBorderStrategy);
    }

    private void injectDependencies(AbstractTransferStrategy strategy) {
        ReflectionTestUtils.setField(strategy, "contextBuilder", contextBuilder);
        ReflectionTestUtils.setField(strategy, "validator", validator);
        ReflectionTestUtils.setField(strategy, "orderDomainService", orderDomainService);
        ReflectionTestUtils.setField(strategy, "accountGateway", accountGateway);
        ReflectionTestUtils.setField(strategy, "stateMachineExecutor", stateMachineExecutor);
    }

    @DisplayName("验证全流程：策略解析 -> Handler执行 -> RPC调用")
    @ParameterizedTest(name = "[{index}] {0}: {1} (RiskPass={2}) -> {3}")
    @CsvSource(value = {
            // === 行内转账 ===
            "INTERNAL, INIT, true, PENDING_RISK",
            "INTERNAL, PENDING_RISK, true, SUCCESS",

            // === 跨境转账 ===
            "CROSS_BORDER, INIT, true, PENDING_RISK",
            "CROSS_BORDER, PENDING_RISK, true, SUCCESS",
            "CROSS_BORDER, PENDING_RISK, false, FAILED"
    }, nullValues = { "null" })
    void shouldExecuteFullFlowIdeally(String strategyTypeStr,
            String initStatusStr,
            boolean isRiskPass,
            String expectedStatusStr) {

        // Given
        TransferType type = TransferType.valueOf(strategyTypeStr);
        TransferStatus initStatus = TransferStatus.valueOf(initStatusStr);
        TransferStatus expectedStatus = TransferStatus.valueOf(expectedStatusStr);

        // 准备订单数据
        String payerNo = "888001";
        String payeeNo = (type == TransferType.INTERNAL) ? "888002" : "FOREIGN_ACC_001";
        String currency = (type == TransferType.INTERNAL) ? "MOP" : "USD";
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal fee = new BigDecimal("10.00"); // 模拟有手续费

        TransferOrder order = new TransferOrder();
        order.setTxnId("TXN_" + System.currentTimeMillis());
        order.setIdempotentKey("KEY_" + System.currentTimeMillis());
        order.setPayerAccountNo(payerNo);
        order.setPayeeAccountNo(payeeNo);
        order.setAmount(amount);
        order.setCurrencyCode(currency);
        order.setFee(fee);
        order.setStatus(initStatus);

        TransferContext context = TransferContext.builder().order(order).build();

        // Mock 依赖行为
        if (type == TransferType.INTERNAL && initStatus == TransferStatus.INIT) {
            AccountSnapshot payee = new AccountSnapshot();
            payee.setAccountNo(payeeNo);
            // 宽容 Mock 任意调用
            lenient().when(accountGateway.getAccount(eq(payeeNo))).thenReturn(payee);
        }

        // === Execution ===
        AbstractTransferStrategy strategy = (type == TransferType.INTERNAL) ? internalStrategy : crossBorderStrategy;

        if (initStatus == TransferStatus.INIT) {
            // 提交阶段：App -> Strategy.execute
            TransferResult result = strategy.execute(context);
            assertEquals(expectedStatus, result.getStatus());
        } else {
            // 回调阶段：StateMachine.drive
            var transition = strategy.getNextTransition(initStatus, isRiskPass);
            stateMachineExecutor.drive(context, transition);
        }

        // === Verification: Verify RPC Calls (Not Mocks!) ===
        // 验证真实 Handler 是否调用了底层 RPC

        // 1. INIT -> PENDING_RISK: 应该 冻结 + 发送风控MQ
        if (initStatus == TransferStatus.INIT) {
            verify(accountGateway, times(1).description("必须调用冻结接口"))
                    .freeze(eq(payerNo), any(), eq(amount), eq(order.getTxnId()), any());

            verify(rocketMQTemplate, times(1).description("必须发送风控消息"))
                    .convertAndSend(anyString(), any(Object.class));

            verify(accountGateway, never()).debit(any(), any(), any(), any(), any(), any()); // 不应扣款
        }

        // 2. PENDING_RISK -> SUCCESS: 应该 扣费 + 扣款(实扣) + 入账(行内only)
        if (initStatus == TransferStatus.PENDING_RISK && isRiskPass) {
            // 2.1 扣费
            verify(accountGateway).debit(eq(payerNo), any(), eq(fee), any(), any(), contains("_FEE"));

            // 2.2 实扣 (unfreezeAndDebit)
            verify(accountGateway).unfreezeAndDebit(eq(payerNo), any(), eq(amount), eq(order.getTxnId()),
                    any(), eq(BizType.TRANSFER_OUT), contains("_PAYER"));

            // 2.3 入账 (仅行内)
            if (type == TransferType.INTERNAL) {
                verify(accountGateway).credit(eq(payeeNo), any(), eq(amount), any(), eq(order.getTxnId()),
                        contains("_PAYEE"));
            } else {
                verify(accountGateway, never()).credit(any(), any(), any(), any(), any(), any());
                // 跨境应该走 NotifySwift (Console Log, 无法 verification mocks unless we spy handler
                // or check logger)
                // 这里主要通过 absence of credit call 验证
            }
        }

        // 3. PENDING_RISK -> FAILED: 应该 解冻
        if (initStatus == TransferStatus.PENDING_RISK && !isRiskPass) {
            verify(accountGateway).unFreeze(eq(payerNo), any(), eq(amount), eq(order.getTxnId()), any());
            verify(accountGateway, never()).unfreezeAndDebit(any(), any(), any(), any(), any(), any(), any());
        }
    }
}
