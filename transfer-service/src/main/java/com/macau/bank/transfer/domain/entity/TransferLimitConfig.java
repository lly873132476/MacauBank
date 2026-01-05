package com.macau.bank.transfer.domain.entity;

import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.transfer.common.enums.ConfigStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 转账限额规则配置表
 */
@Getter
@Setter
@ToString
public class TransferLimitConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 用户等级: NORMAL-普通, KEY-U盾用户, VIP-贵宾
     */
    private String userLevel;

    /**
     * 转账类型: INTERNAL, FPS, SWIFT, CROSS_BORDER
     */
    private TransferType transferType;

    /**
     * 限制币种
     */
    private String currency;

    /**
     * 单笔限额
     */
    private BigDecimal singleLimit;

    /**
     * 日累计限额
     */
    private BigDecimal dailyLimit;

    /**
     * 月累计限额
     */
    private BigDecimal monthlyLimit;

    /**
     * 状态: 1-生效 0-失效
     */
    private ConfigStatusEnum status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}