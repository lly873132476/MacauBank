package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SWIFT 报文通知处理器
 * <p>
 * 职责：向 SWIFT 网关发送跨境转账报文（MT103 等）
 * <p>
 * 阶段：{@link TransferPhaseEnum#NOTIFY_SWIFT}
 * <p>
 * 适用场景：仅用于跨境转账，行内转账不会执行此处理器
 * <p>
 * 当前实现：Mock 实现，实际生产环境需对接真实 SWIFT 网关
 */
@Slf4j
@Component
public class NotifySwiftHandler implements TransferHandler {

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.NOTIFY_SWIFT;
    }

    @Override
    public void handle(TransferContext context) {
        log.info("阶段 [NotifySwift]: 资金处理完毕，向 SWIFT 网关投递报文...");

        // Mock Swift保温投递

        log.info("SWIFT 报文投递成功");
    }
}