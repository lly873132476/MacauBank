package com.macau.bank.transfer.domain.strategy.impl;

import com.macau.bank.common.core.enums.TransferChannel;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.transfer.common.result.TransferErrorCode;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.model.AccountSnapshot;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import com.macau.bank.transfer.domain.statemachine.StateTransition;
import com.macau.bank.transfer.domain.strategy.AbstractTransferStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 普通行内转账策略
 */
@Slf4j
@Component("NORMAL")
public class InternalTransferStrategy extends AbstractTransferStrategy {

    @Override
    public TransferType getTransferType() {
        return TransferType.INTERNAL;
    }

    @Override
    public TransferChannel getTransferChannel(TransferContext context) {
        return TransferChannel.INTERNAL;
    }

    @Override
    protected void doCustomPrepareAndValidate(TransferContext context) {
        // 1. 查询收款账户
        String toAccountNo = context.getOrder().getPayeeAccountNo();

        // 行内转账特有校验：收款人必须是本行账户
        if (!toAccountNo.startsWith("888") && !toAccountNo.startsWith("628888")) {
            throw new BusinessException("收款账户非本行账户，请走跨境转账");
        }

        AccountSnapshot toAccount = null;
        if (toAccountNo.length() >= 16) {
            // 长度超过16位，大概率是卡号
            toAccount = accountGateway.getAccountByCardNo(toAccountNo);
        } else {
            // 长度较短，大概率是账号
            toAccount = accountGateway.getAccount(toAccountNo);
        }

        if (toAccount == null) {
            throw new BusinessException(TransferErrorCode.TO_ACCOUNT_NOT_FOUND);
        }

        // 2. 提前把收款人信息塞进上下文
        context.getOrder().setPayeeAccountNo(toAccount.getAccountNo());
        context.getOrder().setPayeeAccountName(toAccount.getAccountName());
        context.setPayeeAccount(toAccount);
    }

    @Override
    public StateTransition getNextTransition(TransferStatus currentStatus, boolean isRiskPass) {
        // 行内转账可能更简单
        if (currentStatus == TransferStatus.INIT) {
            // 甚至可能不需要发 MQ，直接一步到位（举例）
            // 或者也走风控，但 Handler 不同
            return new StateTransition(
                    List.of(TransferPhaseEnum.FREEZE_FUND, TransferPhaseEnum.SEND_RISK_MQ),
                    TransferStatus.PENDING_RISK
            );
        }

        if (currentStatus == TransferStatus.PENDING_RISK && isRiskPass) {
            return new StateTransition(
                    // 这里的区别是：CreditPayee (直接入账)，没有 NotifySwift
                    List.of(TransferPhaseEnum.DEDUCT_FEE, TransferPhaseEnum.DEDUCT_PAYER, TransferPhaseEnum.CREDIT_PAYEE),
                    TransferStatus.SUCCESS
            );
        }
        // ...
        return null;
    }

}
