package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 手续费冲正处理器
 * <p>
 * 逆向操作：将已扣除的手续费退回付款方账户。
 * <p>
 * 触发条件：
 * - 原订单已扣除手续费
 * - 冲正时需要退还手续费（可配置）
 * <p>
 * 注意：
 * - 部分业务场景下手续费不退还，需根据业务规则决定是否执行此 Handler
 */
@Slf4j
@Component
public class ReverseFeeHandler implements TransferHandler {

    @Resource
    private AccountGateway accountGateway;

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.REVERSE_FEE;
    }

    @Override
    public void handle(TransferContext context) {
        BigDecimal fee = context.getOrder().getFee();

        // 如果没有手续费，跳过
        if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[冲正] 无手续费，跳过手续费冲正: orderId={}",
                    context.getOrder().getId());
            return;
        }

        log.info("[冲正] 开始手续费冲正: orderId={}, payerAccount={}, fee={}",
                context.getOrder().getId(),
                context.getOrder().getPayerInfo().getAccountNo(),
                fee);

        // 构建冲正幂等键
        String reversalIdempotentKey = "REVERSE_FEE:" + context.getOrder().getIdempotentKey();

        // 将手续费退回付款方账户
        accountGateway.credit(
                context.getOrder().getPayerInfo().getAccountNo(),
                context.getOrder().getAmount().getCurrencyCode(),
                fee,
                "冲正退款-手续费冲正",
                context.getOrder().getTxnId(),
                reversalIdempotentKey);

        log.info("[冲正] 手续费冲正成功: orderId={}, 已退回付款方 {} 手续费 {}",
                context.getOrder().getId(),
                context.getOrder().getPayerInfo().getAccountNo(),
                fee);
    }
}
