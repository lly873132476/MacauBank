package com.macau.bank.forex.domain.entity;

import com.macau.bank.common.core.enums.CommonStatus;
import com.macau.bank.common.core.enums.TradeMode;
import com.macau.bank.common.core.enums.YesNo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货币对交易规则配置实体
 */
@Getter
@Setter
@ToString
public class CurrencyPairConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 交易对代码: HKD_MOP, USD_MOP
     */
    private String pairCode;

    /**
     * 基准币种
     */
    private String baseCurrency;

    /**
     * 报价币种
     */
    private String quoteCurrency;

    /**
     * 中文名称: 港币兑澳门元
     */
    private String pairNameCn;

    /**
     * 英文名称: HKD to MOP
     */
    private String pairNameEn;

    /**
     * 业务开关: 1-开盘 0-停盘 (总闸)
     */
    private CommonStatus status;

    /**
     * 单笔最小交易额
     */
    private BigDecimal minAmount;

    /**
     * 单笔最大交易额
     */
    private BigDecimal maxAmount;

    /**
     * 汇率精度
     */
    private Integer ratePrecision;

    /**
     * 是否固定汇率: 1-是 0-否
     */
    private YesNo isPegged;

    /**
     * 交易模式: 1-T+0实时 2-T+1交割
     */
    private TradeMode tradeMode;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
