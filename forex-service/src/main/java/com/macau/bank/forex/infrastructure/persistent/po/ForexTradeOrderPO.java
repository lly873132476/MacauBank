package com.macau.bank.forex.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.forex.common.enums.ForexDirectionEnum;
import com.macau.bank.forex.common.enums.ForexTradeStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 外汇实时交易订单 (PO)
 */
@Getter
@Setter
@ToString
@TableName("forex_trade_order")
public class ForexTradeOrderPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务流水号 (全局唯一, 关联 sub_ledger)
     */
    private String txnId;

    /**
     * 幂等请求ID (前端生成，用于防重复提交)
     */
    private String requestId;

    /**
     * 用户编号
     */
    private String userNo;

    /**
     * 交易对 (如 USD_MOP)
     */
    private String pairCode;

    /**
     * 方向: BUY(用户买入/银行卖出), SELL(用户卖出/银行买入)
     */
    private ForexDirectionEnum direction;

    /**
     * 卖出币种
     */
    private String sellCurrency;

    /**
     * 卖出金额 (保留4位小数以支持内部核算)
     */
    private BigDecimal sellAmount;

    /**
     * 买入币种
     */
    private String buyCurrency;

    /**
     * 买入金额 (保留4位小数以支持内部核算)
     */
    private BigDecimal buyAmount;

    /**
     * 市场基准价 (成本)
     */
    private BigDecimal marketRate;

    /**
     * 客户成交价 (含点差)
     */
    private BigDecimal dealRate;

    /**
     * 银行折算利润 (MOP计价)
     */
    private BigDecimal profitAmountMop;

    /**
     * 0:处理中 1:成功 2:失败 3:冲正(Refounded)
     */
    private ForexTradeStatusEnum status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 是否通过反洗钱/额度检查
     */
    private Boolean riskCheckPass;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
