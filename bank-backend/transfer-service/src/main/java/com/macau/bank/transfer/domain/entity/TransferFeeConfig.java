package com.macau.bank.transfer.domain.entity;

import com.macau.bank.common.core.enums.FeeType;
import com.macau.bank.transfer.common.enums.ConfigStatusEnum;
import com.macau.bank.transfer.common.enums.FeeCalcModeEnum;
import com.macau.bank.transfer.common.enums.FeeDeductTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 转账手续费算费规则
 */
@Getter
@Setter
@ToString
public class TransferFeeConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 渠道: INTERNAL, FPS, SWIFT
     */
    private String transferChannel;

    /**
     * 收费币种
     */
    private String currencyCode;

    /**
     * 适用客群
     */
    private String userLevel;

    /**
     * 适用费用承担方式: SHA, OUR, BEN, ALL
     */
    private FeeType feeType;

    /**
     * 手续费扣款币种
     */
    private String feeCurrency;

    /**
     * 扣费方式: 1-外扣(额外收) 2-内扣(从转账金额中扣除)
     */
    private FeeDeductTypeEnum deductType;

    /**
     * 计费模式: 1-固定金额 2-百分比 3-固定+百分比
     */
    private FeeCalcModeEnum calcMode;

    /**
     * 固定费用
     */
    private BigDecimal fixedAmount;

    /**
     * 费率
     */
    private BigDecimal rate;

    /**
     * 最低收费
     */
    private BigDecimal minFee;

    /**
     * 最高收费(封顶)
     */
    private BigDecimal maxFee;

    private String description;
    private ConfigStatusEnum status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}