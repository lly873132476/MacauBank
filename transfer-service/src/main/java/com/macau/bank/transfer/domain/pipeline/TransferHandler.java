package com.macau.bank.transfer.domain.pipeline;

import com.macau.bank.transfer.domain.context.TransferContext;

/**
 * 转账处理器接口
 * <p>
 * 核心职责：定义转账流程中的单个处理阶段
 * <p>
 * 设计模式：责任链模式 + 管道模式
 * <p>
 * 实现类通过 {@link #getPhase()} 声明自己负责的阶段，
 * 状态机引擎会根据阶段编排顺序依次调用各处理器
 */
public interface TransferHandler {

    /**
     * 执行处理逻辑
     *
     * @param context 转账上下文，包含订单信息和过程数据
     */
    void handle(TransferContext context);

    /**
     * 获取本处理器负责的阶段
     *
     * @return 转账阶段枚举
     */
    TransferPhaseEnum getPhase();
}