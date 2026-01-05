package com.macau.bank.transfer.domain.strategy;

import com.macau.bank.common.core.enums.TransferChannel;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.transfer.application.result.TransferResult;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.statemachine.StateTransition;

import java.math.BigDecimal;

/**
 * 转账策略接口
 * 定义转账操作的通用方法
 */
public interface TransferStrategy {

    /**
     * 获取支持的转账类型
     *
     * @return 转账类型
     */
    TransferType getTransferType();

    /**
     * 获取转账渠道
     *
     * @return 转账渠道
     */
    TransferChannel getTransferChannel(TransferContext context);

    /**
     * 验证转账请求
     *
     * @param context 转账上下文
     * @return 验证是否通过
     */
    boolean validate(TransferContext context);

    /**
     * 执行转账
     *
     * @param context 转账上下文
     * @return 转账结果
     */
    TransferResult execute(TransferContext context);

    /**
     * 计算手续费
     *
     * @param context 转账上下文
     * @return 手续费金额
     */
    BigDecimal calculateFee(TransferContext context);

    /**
     * 获取状态流转配置
     * 这是策略的核心能力之一，必须定义在接口里，这样工厂返回后不用强转就能调。
     */
    StateTransition getNextTransition(TransferStatus currentStatus, boolean isRiskPass);

    /**
     * 获取冲正流程配置
     * <p>
     * 与正向流程对称，每种策略定义自己的冲正路径。
     * 例如：跨境转账可能需要额外通知 SWIFT 取消，行内转账则直接冲正。
     *
     * @param originalStatus 订单原状态（冲正前的状态）
     * @return 状态流转配置（Handler 列表 + 目标状态），返回 null 表示无需冲正
     */
    default StateTransition getReversalTransition(TransferStatus originalStatus) {
        // 默认实现：基于原状态返回通用冲正路径
        // 子类可以重写以提供特定策略的冲正逻辑
        return null;
    }
}
