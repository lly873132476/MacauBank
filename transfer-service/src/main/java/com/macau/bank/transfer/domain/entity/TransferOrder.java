package com.macau.bank.transfer.domain.entity;

import com.macau.bank.common.core.enums.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 转账订单领域实体
 * <p>
 * 核心业务对象，承载转账业务规则与状态
 */
@Getter
@Setter
@ToString
public class TransferOrder implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    /**
     * 本行交易流水号 (全局唯一)
     */
    private String txnId;

    /**
     * 外部通道/清算返回流水号
     */
    private String externalTxnId;

    /**
     * 幂等键 (防重提交)
     */
    private String idempotentKey;
    
    /**
     * 付款方用户编号
     */
    private String userNo;

    /**
     * 付款账户号
     */
    private String payerAccountNo;

    /**
     * 付款账户名
     */
    private String payerAccountName;

    /**
     * 付款账户币种
     */
    private String payerCurrency;
    
    /**
     * 收款账户号
     */
    private String payeeAccountNo;

    /**
     * 收款人姓名
     */
    private String payeeAccountName;

    /**
     * 收款银行代码
     */
    private String payeeBankCode;

    /**
     * SWIFT代码
     */
    private String payeeSwiftCode;

    /**
     * FPS ID (转数快ID)
     */
    private String payeeFpsId;
    
    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易币种
     */
    private String currencyCode;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 手续费承担方式: SHA/OUR/BEN
     */
    private FeeType feeType;
    
    /**
     * 转账类型: NORMAL/CROSS_BORDER/FPS/SWIFT
     */
    private TransferType transferType;

    /**
     * 转账通道: INTERNAL/SWIFT/FPS/CIPS/LOCAL_CLEARING
     */
    private TransferChannel transferChannel;

    /**
     * 跨境用途代码
     */
    private String purposeCode;

    /**
     * 外部参考号 (如 MT103 ID)
     */
    private String referenceNo;

    /**
     * 交易备注/附言
     */
    private String remark;
    
    /**
     * 状态: PROCESSING/SUCCESS/FAILED
     */
    private TransferStatus status;

    /**
     * 失败原因
     */
    private String failReason;
    
    /**
     * 反洗钱状态: PENDING/PASSED/REJECTED/MANUAL_REVIEW/REPORTED
     */
    private AmlStatus amlStatus;

    /**
     * 反洗钱详情
     */
    private String amlDetail;
    
    /**
     * 风控状态: PENDING/PASSED/REJECTED/MANUAL_REVIEW
     */
    private RiskStatus riskStatus;

    /**
     * 风控详情
     */
    private String riskDetail;
    
    /**
     * 扩展字段 (JSON)
     */
    private String extendInfo;
    
    private Deleted deleted;

    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
