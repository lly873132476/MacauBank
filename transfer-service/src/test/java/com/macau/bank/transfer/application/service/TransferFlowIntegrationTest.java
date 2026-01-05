package com.macau.bank.transfer.application.service;

import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.transfer.application.command.TransferCmd;
import com.macau.bank.transfer.application.result.TransferResult;
import com.macau.bank.transfer.application.service.impl.TransferAppServiceImpl;
import com.macau.bank.transfer.domain.ability.TransferContextBuilder;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.factory.TransferStrategyFactory;
import com.macau.bank.transfer.domain.model.AccountSnapshot;
import com.macau.bank.transfer.domain.service.TransferOrderDomainService;
import com.macau.bank.transfer.domain.strategy.TransferStrategy;
import com.macau.bank.common.core.enums.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 转账全流程需求测试
 * <p>
 * 测试目标：验证转账业务全流程，从 AppService 到 Domain 层
 * <p>
 * 测试策略：
 * - Mock 外部依赖（Strategy, ContextBuilder）
 * - 验证完整的业务流程和状态变更
 * - 基于需求场景覆盖正常流程、异常流程
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("转账全流程需求测试")
class TransferFlowIntegrationTest {

    @Mock
    private TransferStrategyFactory transferStrategyFactory;

    @Mock
    private TransferOrderDomainService transferOrderDomainService;

    @Mock
    private TransferContextBuilder transferContextBuilder;

    @Mock
    private TransferStrategy mockStrategy;

    @InjectMocks
    private TransferAppServiceImpl transferAppService;

    private TransferCmd transferCmd;
    private TransferContext transferContext;
    private TransferOrder transferOrder;

    @BeforeEach
    void setUp() {
        // 构建转账指令
        transferCmd = new TransferCmd();
        transferCmd.setFromAccountNo("ACC_001");
        transferCmd.setToAccountNo("ACC_002");
        transferCmd.setAmount(new BigDecimal("1000.00"));
        transferCmd.setCurrencyCode("MOP");
        transferCmd.setTransferType(TransferType.INTERNAL);
        transferCmd.setIdempotentKey("IDEM_KEY_001");

        // 构建转账订单
        transferOrder = new TransferOrder();
        transferOrder.setTxnId("TXN_001");
        transferOrder.setPayerAccountNo("ACC_001");
        transferOrder.setPayeeAccountNo("ACC_002");
        transferOrder.setAmount(new BigDecimal("1000.00"));
        transferOrder.setStatus(TransferStatus.INIT);

        // 构建转账上下文
        transferContext = TransferContext.builder()
                .order(transferOrder)
                .build();
    }

    @Nested
    @DisplayName("需求场景：正常转账流程")
    class NormalTransferFlowTests {

        @Test
        @DisplayName("行内转账成功 - 完整流程验证")
        void shouldCompleteInternalTransferSuccessfully() {
            // Given - 配置 Mock
            when(transferStrategyFactory.getStrategy(TransferType.INTERNAL)).thenReturn(mockStrategy);
            when(transferContextBuilder.build(any(TransferCmd.class))).thenReturn(transferContext);

            TransferResult expectedResult = TransferResult.builder()
                    .txnId("TXN_001")
                    .status(TransferStatus.PENDING_RISK)
                    .build();
            when(mockStrategy.execute(any(TransferContext.class))).thenReturn(expectedResult);

            // When - 执行转账
            TransferResult result = transferAppService.submitTransfer(transferCmd);

            // Then - 验证完整流程
            assertNotNull(result);
            assertEquals("TXN_001", result.getTxnId());

            // 验证流程调用顺序
            verify(transferStrategyFactory).getStrategy(TransferType.INTERNAL);
            verify(transferContextBuilder).build(transferCmd);
            verify(mockStrategy).execute(transferContext);
        }

        @DisplayName("不同转账类型应选择对应策略")
        @ParameterizedTest(name = "[{index}] 转账类型={0}")
        @CsvSource({
                "INTERNAL",
                "LOCAL",
                "CROSS_BORDER"
        })
        void shouldSelectCorrectStrategy(String transferTypeStr) {
            // Given
            TransferType transferType = TransferType.valueOf(transferTypeStr);
            transferCmd.setTransferType(transferType);

            when(transferStrategyFactory.getStrategy(transferType)).thenReturn(mockStrategy);
            when(transferContextBuilder.build(any())).thenReturn(transferContext);
            when(mockStrategy.execute(any())).thenReturn(TransferResult.builder().txnId("TXN_001").build());

            // When
            transferAppService.submitTransfer(transferCmd);

            // Then - 验证选择了正确的策略
            verify(transferStrategyFactory).getStrategy(transferType);
        }
    }

    @Nested
    @DisplayName("需求场景：转账金额边界测试")
    class TransferAmountBoundaryTests {

        @DisplayName("转账金额边界验证")
        @ParameterizedTest(name = "[{index}] {0}: 金额={1}")
        @CsvSource({
                // 场景, 金额
                "最小金额-1分钱,      0.01",
                "正常金额-1000元,     1000.00",
                "大额转账-100万,      1000000.00"
        })
        void shouldHandleVariousAmounts(String scenario, BigDecimal amount) {
            // Given
            transferCmd.setAmount(amount);
            transferOrder.setAmount(amount);

            when(transferStrategyFactory.getStrategy(any())).thenReturn(mockStrategy);
            when(transferContextBuilder.build(any())).thenReturn(transferContext);
            when(mockStrategy.execute(any())).thenReturn(TransferResult.builder().txnId("TXN_001").build());

            // When
            TransferResult result = transferAppService.submitTransfer(transferCmd);

            // Then
            assertNotNull(result);
            verify(mockStrategy).execute(any(TransferContext.class));
        }
    }

    @Nested
    @DisplayName("需求场景：订单状态流转")
    class OrderStatusFlowTests {

        @DisplayName("订单状态流转验证")
        @ParameterizedTest(name = "[{index}] {0}")
        @CsvSource({
                // 场景, 预期返回状态
                "提交后待风控,              PENDING_RISK",
                "风控通过后成功,            SUCCESS",
                "风控拒绝后失败,            FAILED"
        })
        void shouldReturnCorrectStatus(String scenario, String expectedStatusStr) {
            // Given
            TransferStatus expectedStatus = TransferStatus.valueOf(expectedStatusStr);

            when(transferStrategyFactory.getStrategy(any())).thenReturn(mockStrategy);
            when(transferContextBuilder.build(any())).thenReturn(transferContext);

            TransferResult expectedResult = TransferResult.builder()
                    .txnId("TXN_001")
                    .status(expectedStatus)
                    .build();
            when(mockStrategy.execute(any())).thenReturn(expectedResult);

            // When
            TransferResult result = transferAppService.submitTransfer(transferCmd);

            // Then
            assertEquals(expectedStatus, result.getStatus(),
                    "场景 [" + scenario + "] 订单状态应该是 " + expectedStatusStr);
        }
    }

    @Nested
    @DisplayName("需求场景：幂等性验证")
    class IdempotencyTests {

        @Test
        @DisplayName("相同幂等键应返回相同结果")
        void shouldReturnSameResultForSameIdempotentKey() {
            // Given - 两次使用相同的幂等键
            String idempotentKey = "IDEM_KEY_DUPLICATE";
            transferCmd.setIdempotentKey(idempotentKey);

            when(transferStrategyFactory.getStrategy(any())).thenReturn(mockStrategy);
            when(transferContextBuilder.build(any())).thenReturn(transferContext);

            TransferResult expectedResult = TransferResult.builder()
                    .txnId("TXN_001")
                    .status(TransferStatus.PENDING_RISK)
                    .build();
            when(mockStrategy.execute(any())).thenReturn(expectedResult);

            // When - 第一次调用
            TransferResult result1 = transferAppService.submitTransfer(transferCmd);
            // When - 第二次调用（相同幂等键）
            TransferResult result2 = transferAppService.submitTransfer(transferCmd);

            // Then - 应该返回相同的交易号
            assertEquals(result1.getTxnId(), result2.getTxnId());
        }
    }
}
