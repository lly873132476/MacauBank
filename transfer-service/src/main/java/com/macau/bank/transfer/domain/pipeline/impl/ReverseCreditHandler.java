package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 入账冲正处理器
 * <p>
 * 逆向操作：从收款方账户扣回已入账的资金。
 * <p>
 * 触发条件：
 * - 原订单已成功入账到收款方
 * - 需要冲正退款时执行
 * <p>
 * 幂等性保证：
 * - 使用 "REVERSE_CREDIT:" + 原订单幂等键 作为冲正幂等键
 * - 防止重复扣款
 */
@Slf4j
@Component
public class ReverseCreditHandler implements TransferHandler {

    @Resource
    private AccountGateway accountGateway;

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.REVERSE_CREDIT;
    }

    @Override
    public void handle(TransferContext context) {
        log.info("[冲正] 开始入账冲正: orderId={}, payeeAccount={}, amount={}",
                context.getOrder().getId(),
                context.getOrder().getPayeeAccountNo(),
                context.getOrder().getAmount());

        // 构建冲正幂等键
        String reversalIdempotentKey = "REVERSE_CREDIT:" + context.getOrder().getIdempotentKey();

        // 从收款方账户扣回资金
        accountGateway.debit(
                context.getOrder().getPayeeAccountNo(),
                context.getOrder().getCurrencyCode(),
                context.getOrder().getAmount(),
                "冲正退款-入账冲正",
                context.getOrder().getTxnId(),
                reversalIdempotentKey);

        log.info("[冲正] 入账冲正成功: orderId={}, 已从收款方 {} 扣回 {}",
                context.getOrder().getId(),
                context.getOrder().getPayeeAccountNo(),
                context.getOrder().getAmount());
    }
}
