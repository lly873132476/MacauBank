package com.macau.bank.forex.domain.entity;

import com.macau.bank.forex.common.enums.PositionStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 银行自营头寸表
 */
@Getter
@Setter
@ToString
public class BankPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 外币代码 (USD, HKD...)
     */
    private String currencyCode;

    /**
     * 总持仓 (包含冻结)
     */
    private BigDecimal totalAmount;

    /**
     * 冻结/占用金额 (交易进行中)
     */
    private BigDecimal frozenAmount;

    /**
     * 持仓均价 (相对于基准币种 MOP)
     */
    private BigDecimal averageCost;

    /**
     * 持仓上限 (多头限制)
     */
    private BigDecimal riskLimitMax;

    /**
     * 持仓下限 (空头限制)
     */
    private BigDecimal riskLimitMin;

    /**
     * 1:正常 0:暂停交易(熔断)
     */
    private PositionStatus status;

    /**
     * 乐观锁版本号
     */
    private Long version;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
