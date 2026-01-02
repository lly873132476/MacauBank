package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 扣除手续费处理器
 * <p>
 * 职责：从付款方账户扣除转账手续费
 * <p>
 * 阶段：{@link TransferPhaseEnum#DEDUCT_FEE}
 * <p>
 * 设计说明：
 * - 如果手续费为 0 或 null，则跳过此阶段
 * - 手续费根据转账类型和金额由策略计算得出
 * - 使用独立的幂等键（txnId + "_FEE"）确保手续费不会重复扣除
 */
@Slf4j
@Component
public class DeductFeeHandler implements TransferHandler {

    @Resource
    private AccountGateway accountGateway;

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.DEDUCT_FEE;
    }

    @Override
    public void handle(TransferContext context) {
        BigDecimal fee = context.getOrder().getFee();
        if (fee == null || fee.compareTo(BigDecimal.ZERO) == 0) {
            log.info("免手续费，跳过 DeductFee 阶段");
            return;
        }

        log.info("阶段 [DeductFee]: 开始扣除手续费: {}", fee);

        // 这里假设手续费是立即扣除的
        TransferOrder order = context.getOrder();
        accountGateway.debit(order.getPayerAccountNo(), order.getCurrencyCode(), fee, "转账手续费", order.getTxnId(),
                order.getIdempotentKey() + "_FEE");

    }
}