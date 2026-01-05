package com.macau.bank.transfer.domain.strategy.impl;

import com.macau.bank.common.core.enums.TransferChannel;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.statemachine.StateTransition;
import com.macau.bank.transfer.domain.strategy.AbstractTransferStrategy;
import org.springframework.stereotype.Component;

@Component
public class LocalTransferStrategy extends AbstractTransferStrategy {

    @Override
    public TransferType getTransferType() {
        return TransferType.LOCAL;
    }

    @Override
    public TransferChannel getTransferChannel(TransferContext context) {
        return TransferChannel.FPS; // 指定走 FPS 通道
    }

    @Override
    public StateTransition getNextTransition(TransferStatus status, boolean isPass) {
        // FPS 通常也是实时的，流程可能类似于：
        // Freeze -> Risk -> Deduct -> CallFpsGateway
        // ...
        return null;
    }
}