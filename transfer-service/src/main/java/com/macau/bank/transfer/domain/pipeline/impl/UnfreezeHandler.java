package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 解冻资金处理器
 * <p>
 * 职责：交易失败或取消时释放已冻结的资金（TCC Cancel 阶段）
 * <p>
 * 阶段：{@link TransferPhaseEnum#UNFREEZE}
 * <p>
 * 触发场景：风控拒绝、用户取消、系统异常等
 */
@Slf4j
@Component
public class UnfreezeHandler implements TransferHandler {

    @Resource
    private AccountGateway accountGateway;

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.UNFREEZE;
    }

    @Override
    public void handle(TransferContext context) {
        log.info("阶段 [Unfreeze]: 交易终止，执行 TCC Cancel (解冻), txnId={}", context.getOrder().getAmount().getCurrencyCode());

        // 解冻资金
        TransferOrder order = context.getOrder();
        accountGateway.unFreeze(
                context.getOrder().getPayerInfo().getAccountNo(),
                context.getOrder().getAmount().getCurrencyCode(),
                order.getAmount().getAmount(),
                order.getTxnId(),
                "转账资金解冻");
    }
}