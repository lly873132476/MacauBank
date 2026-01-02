package com.macau.bank.forex.application.service.impl;

import com.macau.bank.api.account.request.CreditRpcRequest;
import com.macau.bank.api.account.request.DebitRpcRequest;
import com.macau.bank.api.account.service.AccountRpcService;
import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.Currency;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.forex.application.assembler.ForexDomainAssembler;
import com.macau.bank.forex.application.command.ExchangeCmd;
import com.macau.bank.forex.application.result.ExchangeResult;
import com.macau.bank.forex.application.service.ForexAppService;
import com.macau.bank.forex.common.enums.ForexTradeStatusEnum;
import com.macau.bank.forex.common.result.ForexErrorCode;
import com.macau.bank.forex.domain.entity.CurrencyPairConfig;
import com.macau.bank.forex.domain.entity.ForexTradeOrder;
import com.macau.bank.forex.domain.service.CurrencyDomainService;
import com.macau.bank.forex.domain.service.ForexDomainService;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ForexAppServiceImpl implements ForexAppService {

    /** 本币代码 */
    private static final String LOCAL_CURRENCY = Currency.MOP.getCode();

    @Resource
    private ForexDomainService forexDomainService;

    @DubboReference
    private AccountRpcService accountRpcService;

    @Resource
    private CurrencyDomainService currencyDomainService;

    @Resource
    private ForexDomainAssembler forexDomainAssembler;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    @Lazy
    private ForexAppService self;

    /**
     * 银行外汇内部户账号 (用于 MOP 本币调账)
     */
    @org.springframework.beans.factory.annotation.Value("${bank.internal-account.forex-account-no}")
    private String bankForexInternalAccountNo;

    @Override
    public ExchangeResult exchange(ExchangeCmd cmd) {
        log.info("应用服务 - 执行外币兑换: userNo={}, pair={}, requestId={}",
                cmd.getUserNo(), cmd.getPairCode(), cmd.getRequestId());

        // --- 0. 安全校验 (Security Validation) ---

        // 0.1 幂等性校验：检查 requestId 是否已存在
        ForexTradeOrder existingOrder = forexDomainService.findByRequestId(cmd.getRequestId());
        if (existingOrder != null) {
            log.warn("重复请求被拦截: requestId={}, 已有订单txnId={}", cmd.getRequestId(), existingOrder.getTxnId());
            // 如果已有成功的订单，直接返回；否则抛出异常
            if (existingOrder.getStatus() == ForexTradeStatusEnum.SUCCESS) {
                return forexDomainAssembler.toResult(existingOrder);
            }
            throw new BusinessException(ForexErrorCode.DUPLICATE_REQUEST);
        }

        // 0.2 账户归属校验：确保账户属于当前用户
        Result<Boolean> ownerResult = accountRpcService.validateAccountOwnership(cmd.getAccountNo(), cmd.getUserNo());
        if (ownerResult == null || !ownerResult.isSuccess() || !Boolean.TRUE.equals(ownerResult.getData())) {
            log.warn("账户归属校验失败: accountNo={}, userNo={}, result={}", cmd.getAccountNo(), cmd.getUserNo(), ownerResult);
            throw new BusinessException(ForexErrorCode.ACCOUNT_NOT_BELONG_TO_USER);
        }

        // --- 1. 准备数据 (Data Preparation) ---

        // 1.1 获取并校验配置 (本地DB)
        CurrencyPairConfig pairConfig = forexDomainService.getAndValidateConfig(cmd.getPairCode());

        // 1.2 获取交易汇率 (比如 USD/CNY)
        // 这里的汇率用于计算用户能换多少钱
        BigDecimal marketRate = currencyDomainService.getExchangeRate(pairConfig.getBaseCurrency(),
                pairConfig.getQuoteCurrency());

        // 1.3 【关键】预先获取成本核算汇率
        // 这一步是为了 updateBankPosition 能够算出准确的 MOP 成本
        // 实际生产中建议使用 CompletableFuture 并行获取，或者 RPC 提供批量接口
        BigDecimal sellCurrencyMopRate = currencyDomainService.getExchangeRate(cmd.getSellCurrency(),
                Currency.MOP.getCode());
        BigDecimal buyCurrencyMopRate = currencyDomainService.getExchangeRate(cmd.getBuyCurrency(),
                Currency.MOP.getCode());

        // 🔥 关键：找出哪个是 Quote Currency，并取它的 MOP 汇率
        BigDecimal quoteToMopRate;
        if (cmd.getSellCurrency().equals(pairConfig.getQuoteCurrency())) {
            quoteToMopRate = sellCurrencyMopRate;
        } else {
            quoteToMopRate = buyCurrencyMopRate;
        }

        // --- 2. 核心业务 (Domain Execution) ---

        // 2.1 创建本地订单 (处理中)
        ForexTradeOrder order = forexDomainService.createOrder(
                cmd.getRequestId(), cmd.getUserNo(), cmd.getPairCode(), cmd.getSellCurrency(),
                cmd.getSellAmount(), cmd.getBuyCurrency(), pairConfig, marketRate, quoteToMopRate);

        try {
            // 2.2 开启分布式事务执行资金交割
            self.doGlobalTransaction(cmd, order, sellCurrencyMopRate, buyCurrencyMopRate);

            // 2.3 成功返回
            return forexDomainAssembler.toResult(order);

        } catch (Exception e) {
            // 2.4 失败兜底更新状态
            log.error("交易失败，记录留痕");
            forexDomainService.updateOrderStatus(order.getTxnId(), ForexTradeStatusEnum.FAIL, e.getMessage());
            throw e; // 抛给前端看
        }

    }

    @GlobalTransactional(name = "forex-exchange-tx", rollbackFor = Exception.class)
    public void doGlobalTransaction(ExchangeCmd cmd, ForexTradeOrder order, BigDecimal sellCurrencyMopRate,
            BigDecimal buyCurrencyMopRate) {

        try {
            // 4. 资金扣减 (卖出币种)
            DebitRpcRequest sellRequest = DebitRpcRequest.builder()
                    .accountNo(cmd.getAccountNo())
                    .currencyCode(cmd.getSellCurrency())
                    .amount(cmd.getSellAmount()) // 金额为正数
                    .description("外币兑换卖出: " + cmd.getPairCode())
                    .bizNo(order.getTxnId())
                    .requestId(cmd.getRequestId() + "_SELL")
                    .build();
            Result<Boolean> debitSuccess = accountRpcService.debit(sellRequest);
            if (!debitSuccess.isSuccess())
                throw new BusinessException(debitSuccess.getCode(), debitSuccess.getMessage());

            // 5. 资金增加 (买入币种)
            CreditRpcRequest buyRequest = CreditRpcRequest.builder()
                    .accountNo(cmd.getAccountNo())
                    .currencyCode(cmd.getBuyCurrency())
                    .amount(order.getBuyAmount()) // 金额为正数
                    .description("外币兑换买入: " + cmd.getPairCode())
                    .bizNo(order.getTxnId())
                    .requestId(order.getRequestId() + "_BUY")
                    .build();
            Result<Boolean> creditSuccess = accountRpcService.credit(buyRequest);
            if (!creditSuccess.isSuccess())
                throw new BusinessException(creditSuccess.getCode(), creditSuccess.getMessage());

            // 6. 更新银行资金 (区分本币和外币)
            // 用户卖出的币种 -> 银行获得
            updateBankFunds(cmd.getSellCurrency(), cmd.getSellAmount(), sellCurrencyMopRate,
                    order.getTxnId(), order.getRequestId() + "_BANK_IN", "外汇交易银行收入: " + cmd.getPairCode());
            // 用户买入的币种 -> 银行支出
            updateBankFunds(cmd.getBuyCurrency(), order.getBuyAmount().negate(), buyCurrencyMopRate,
                    order.getTxnId(), order.getRequestId() + "_BANK_OUT", "外汇交易银行支出: " + cmd.getPairCode());

            // 7. 更新订单状态为成功
            forexDomainService.updateOrderStatus(order.getTxnId(), ForexTradeStatusEnum.SUCCESS, null);
            order.setStatus(ForexTradeStatusEnum.SUCCESS);

            // 8. 发送异步通知消息
            sendForexMessage(order);

        } catch (BusinessException e) {
            log.warn("外币兑换业务失败: {}", e.getMessage());
            throw e;// 抛出异常触发 Seata 回滚
        } catch (Exception e) {
            log.error("外币兑换系统异常: {}", e.getMessage());
            throw e; // 抛出异常触发 Seata 回滚
        }

    }

    /**
     * 更新银行资金
     * - 本币(MOP)：通过内部户账户调账
     * - 外币：更新银行头寸表
     *
     * @param currency    币种
     * @param amount      金额（正数=银行收入，负数=银行支出）
     * @param rateToMop   该币种对 MOP 的汇率
     * @param requestId   幂等ID
     * @param description 描述
     */
    private void updateBankFunds(String currency, BigDecimal amount, BigDecimal rateToMop,
            String bizNo, String requestId, String description) {
        if (LOCAL_CURRENCY.equals(currency)) {
            // 本币 MOP：通过银行内部户调账
            log.info("本币调账: currency={}, amount={}, internalAccount={}", currency, amount, bankForexInternalAccountNo);

            // 根据正负决定是扣款还是入账
            Result<Boolean> result;
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                // 负数 = 扣款
                DebitRpcRequest request = DebitRpcRequest.builder()
                        .accountNo(bankForexInternalAccountNo)
                        .currencyCode(currency)
                        .amount(amount.abs()) // 转为正数
                        .description(description)
                        .bizNo(bizNo)
                        .requestId(requestId)
                        .build();
                result = accountRpcService.debit(request);
            } else {
                // 正数 = 入账
                CreditRpcRequest request = CreditRpcRequest.builder()
                        .accountNo(bankForexInternalAccountNo)
                        .currencyCode(currency)
                        .amount(amount)
                        .description(description)
                        .bizNo(bizNo)
                        .requestId(requestId)
                        .build();
                result = accountRpcService.credit(request);
            }

            if (!result.isSuccess()) {
                throw new BusinessException(result.getCode(), "内部户调账失败: " + result.getMessage());
            }
        } else {
            // 外币：更新银行头寸表
            log.info("外币头寸更新: currency={}, amount={}", currency, amount);
            forexDomainService.updateBankPosition(currency, amount, rateToMop);
        }
    }

    private void sendForexMessage(ForexTradeOrder order) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("userNo", order.getUserNo());
            msg.put("txnId", order.getTxnId());
            msg.put("bizType", BizType.FOREX_EXCHANGE);
            msg.put("amount", order.getBuyAmount());
            msg.put("currency", order.getBuyCurrency());
            rocketMQTemplate.convertAndSend("BANK_TXN_EVENT_TOPIC", msg);
        } catch (Exception e) {
            log.warn("发送外汇MQ消息失败: {}", e.getMessage());
        }
    }

}
