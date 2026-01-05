package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 扣款冲正处理器
 * <p>
 * 逆向操作：将已扣款的资金退回付款方账户。
 * <p>
 * 触发条件：
 * - 原订单已从付款方扣款
 * - 需要冲正退款时执行
 * <p>
 * 幂等性保证：
 * - 使用 "REVERSE_DEDUCT:" + 原订单幂等键 作为冲正幂等键
 * - 防止重复退款
 */
@Slf4j
@Component
public class ReverseDeductHandler implements TransferHandler {

    @Resource
    private AccountGateway accountGateway;

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.REVERSE_DEDUCT;
    }

    @Override
    public void handle(TransferContext context) {
        log.info("[冲正] 开始扣款冲正: orderId={}, payerAccount={}, amount={}",
                context.getOrder().getId(),
                context.getOrder().getPayerAccountNo(),
                context.getOrder().getAmount());

        // 构建冲正幂等键
        String reversalIdempotentKey = "REVERSE_DEDUCT:" + context.getOrder().getIdempotentKey();

        // 将资金退回付款方账户
        accountGateway.credit(
                context.getOrder().getPayerAccountNo(),
                context.getOrder().getCurrencyCode(),
                context.getOrder().getAmount(),
                "冲正退款-扣款冲正",
                context.getOrder().getTxnId(),
                reversalIdempotentKey);

        log.info("[冲正] 扣款冲正成功: orderId={}, 已退回付款方 {} 金额 {}",
                context.getOrder().getId(),
                context.getOrder().getPayerAccountNo(),
                context.getOrder().getAmount());
    }
}
