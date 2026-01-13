package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 扣款处理器
 * <p>
 * 职责：解冻并扣除付款方资金（TCC Confirm 阶段）
 * <p>
 * 阶段：{@link TransferPhaseEnum#DEDUCT_PAYER}
 * <p>
 * 设计说明：
 * - 调用 {@link AccountGateway#unfreezeAndDebit} 原子操作
 * - 传入 txnId 用于关联冻结记录
 * - 业务类型固定为 TRANSFER_OUT（转出）
 */
@Slf4j
@Component
public class DeductMoneyHandler implements TransferHandler {

    @Resource
    private AccountGateway accountGateway;

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.DEDUCT_PAYER;
    }

    @Override
    public void handle(TransferContext context) {
        log.info("阶段 [DeductMoney]: 风控通过，执行 TCC Confirm (实扣), txnId={}",
                context.getOrder().getPayerInfo().getAccountNo());

        // 调用 TCC Confirm 接口
        // 关键点：传 txnId，Account 服务会根据 txnId 找到那条 "FROZEN" 记录，并将其核销
        TransferOrder order = context.getOrder();
        accountGateway.unfreezeAndDebit(
                context.getOrder().getPayerInfo().getAccountNo(),
                context.getOrder().getAmount().getCurrencyCode(),
                order.getAmount().getAmount(),
                order.getTxnId(), "转账资金解冻", BizType.TRANSFER_OUT, order.getIdempotentKey() + "_PAYER");
    }
}