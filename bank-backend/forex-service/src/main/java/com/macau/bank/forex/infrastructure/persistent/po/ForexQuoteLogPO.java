package com.macau.bank.forex.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易报价审计日志 (PO)
 */
@Getter
@Setter
@ToString
@TableName("forex_quote_log")
public class ForexQuoteLogPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联交易单号
     */
    private String txnId;

    /**
     * 报价时间(精确到毫秒)
     */
    private LocalDateTime quoteTime;

    /**
     * 交易对代码
     */
    private String pairCode;

    /**
     * 银行卖出价
     */
    private BigDecimal askPrice;

    /**
     * 银行买入价
     */
    private BigDecimal bidPrice;

    /**
     * 中间价
     */
    private BigDecimal midPrice;

    /**
     * 报价来源
     */
    private String sourceSystem;

    /**
     * 点差配置版本号
     */
    private String spreadVersion;
}
