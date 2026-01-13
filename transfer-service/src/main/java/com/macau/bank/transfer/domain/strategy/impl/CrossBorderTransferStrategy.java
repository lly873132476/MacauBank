package com.macau.bank.transfer.domain.strategy.impl;

import com.macau.bank.common.core.enums.Currency;
import com.macau.bank.common.core.enums.TransferChannel;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.transfer.common.result.TransferErrorCode;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import com.macau.bank.transfer.domain.statemachine.StateTransition;
import com.macau.bank.transfer.domain.strategy.AbstractTransferStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 跨境转账策略 (CROSS_BORDER)
 */
@Slf4j
@Component("CROSS_BORDER")
public class CrossBorderTransferStrategy extends AbstractTransferStrategy {

    @Override
    public TransferType getTransferType() {
        return TransferType.CROSS_BORDER;
    }

    @Override
    public TransferChannel getTransferChannel(TransferContext context) {
        if (Currency.CNY.getCode().equals(context.getOrder().getAmount().getCurrencyCode())) {
            return TransferChannel.CIPS; // 人民币走 CIPS
        }
        return TransferChannel.SWIFT;
    }

    @Override
    protected void doCustomPrepareAndValidate(TransferContext context) {
        if (!StringUtils.hasText(context.getOrder().getPayeeInfo().getAccountNo())) {
            throw new BusinessException(TransferErrorCode.TO_ACCOUNT_NOT_NULL);
        }
        // TODO: 校验 SWIFT Code 等跨境必须参数
    }

    @Override
    public StateTransition getNextTransition(TransferStatus currentStatus, boolean isRiskPass) {
        // 1. 初始阶段
        if (currentStatus == TransferStatus.INIT) {
            return new StateTransition(
                    List.of(TransferPhaseEnum.FREEZE_FUND, TransferPhaseEnum.SEND_RISK_MQ), // 动作
                    TransferStatus.PENDING_RISK // 目标状态
            );
        }

        // 2. 风控回调阶段
        if (currentStatus == TransferStatus.PENDING_RISK) {
            if (isRiskPass) {
                return new StateTransition(
                        List.of(TransferPhaseEnum.DEDUCT_FEE, TransferPhaseEnum.DEDUCT_PAYER,
                                TransferPhaseEnum.NOTIFY_SWIFT),
                        TransferStatus.SUCCESS);
            } else {
                return new StateTransition(
                        List.of(TransferPhaseEnum.UNFREEZE),
                        TransferStatus.FAILED);
            }
        }
        return null;
    }

}
