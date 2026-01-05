package com.macau.bank.forex.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.forex.common.enums.RateStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 汇率表 (PO)
 */
@Getter
@Setter
@ToString
@TableName("exchange_rate")
public class ExchangeRatePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 货币对代码 (如: HKD_MOP, CNY_MOP, USD_MOP)
     */
    private String currencyPair;

    /**
     * 基准货币 (如: HKD)
     */
    private String baseCurrency;

    /**
     * 目标货币 (如: MOP)
     */
    private String targetCurrency;

    /**
     * 银行买入价 (用户卖出基准货币)
     */
    private BigDecimal bankBuyRate;

    /**
     * 银行卖出价 (用户买入基准货币)
     */
    private BigDecimal bankSellRate;

    /**
     * 中间价 (用于内部记账/统计)
     */
    private BigDecimal middleRate;

    /**
     * 基准货币单位 (通常为1，日元可能为100)
     */
    private Integer unit;

    /**
     * 状态: 1-生效 0-失效
     */
    private RateStatusEnum status;

    /**
     * 生效时间
     */
    private LocalDateTime effectTime;

    /**
     * 失效时间 (新汇率进来时，更新旧记录的失效时间)
     */
    private LocalDateTime expireTime;

    /**
     * 来源: BLOOMBERG/REUTERS/MANUAL
     */
    private String sourceSystem;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
