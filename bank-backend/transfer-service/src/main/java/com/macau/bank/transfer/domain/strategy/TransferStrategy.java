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
}
