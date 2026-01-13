package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.common.core.exception.SystemException;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 冻结资金处理器
 * <p>
 * 职责：调用账户服务冻结付款方指定金额
 * <p>
 * 阶段：{@link TransferPhaseEnum#FREEZE_FUND}
 * <p>
 * 设计说明：作为 TCC Try 阶段的一部分，确保资金预留
 */
@Slf4j
@Component
public class FreezeFundHandler implements TransferHandler {

    @Resource
    private AccountGateway accountGateway; // Feign 或 Dubbo 客户端

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.FREEZE_FUND;
    }

    @Override
    public void handle(TransferContext context) {
        log.info("开始请求账户服务进行冻结: txnId={}", context.getOrder().getTxnId());
        accountGateway.freeze(context.getOrder().getPayerInfo().getAccountNo(),
                context.getOrder().getPayerInfo().getCurrency(),
                context.getAmount(), context.getOrder().getTxnId(), "转账资金冻结");
    }
}