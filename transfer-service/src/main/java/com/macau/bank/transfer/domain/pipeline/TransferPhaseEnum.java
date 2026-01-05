package com.macau.bank.transfer.domain.pipeline;

/**
 * 转账阶段枚举
 * <p>
 * 定义转账流程中的所有处理阶段，用于管道模式中的阶段调度
 * <p>
 * 设计说明：
 * - 每个枚举值对应一个 {@link TransferHandler} 实现类
 * - 状态机引擎根据策略配置的阶段列表依次执行
 */
public enum TransferPhaseEnum {

    /** 冻结资金 - 调用账户服务冻结付款方余额 */
    FREEZE_FUND,

    /** 发送风控消息 - 发送 MQ 消息至风控系统进行异步校验 */
    SEND_RISK_MQ,

    /** 扣除手续费 - 从付款方账户扣除交易手续费 */
    DEDUCT_FEE,

    /** 扣款 - 解冻并扣除付款方资金（TCC Confirm） */
    DEDUCT_PAYER,

    /** 入账 - 将资金存入收款方账户 */
    CREDIT_PAYEE,

    /** 发送 SWIFT 报文 - 跨境转账时发送国际清算报文 */
    NOTIFY_SWIFT,

    /** 解冻资金 - 交易失败或取消时释放冻结资金（TCC Cancel） */
    UNFREEZE,

    // ==================== 冲正阶段（逆向流程） ====================

    /** 入账冲正 - 从收款方账户扣回资金 */
    REVERSE_CREDIT,

    /** 扣款冲正 - 将资金退回付款方账户 */
    REVERSE_DEDUCT,

    /** 手续费冲正 - 退还手续费（可选） */
    REVERSE_FEE
}