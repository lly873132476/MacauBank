package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 收款人入账处理器
 * <p>
 * 职责：将转账金额存入收款方账户
 * <p>
 * 阶段：{@link TransferPhaseEnum#CREDIT_PAYEE}
 * <p>
 * 适用场景：行内转账时直接入账，跨境转账则不使用此处理器
 */
@Slf4j
@Component
public class CreditPayeeHandler implements TransferHandler {

    @Resource
    private AccountGateway accountGateway;

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.CREDIT_PAYEE; // 新增枚举：收款人入账
    }

    @Override
    public void handle(TransferContext context) {
        log.info("阶段 [CreditPayee]: 行内转账，准备给收款人入账...");

        // 直接调用 Account 服务给收款人加钱
        accountGateway.credit(
                context.getOrder().getPayeeAccountNo(),
                context.getOrder().getCurrencyCode(),
                context.getAmount(),
                "行内转账收款",
                context.getOrder().getTxnId(),
                context.getOrder().getIdempotentKey() + "_PAYEE" // 幂等键
        );
    }
}