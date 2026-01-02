package com.macau.bank.transfer.domain.message;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 风控请求消息体
 * <p>
 * 属于领域层定义的“契约”，基础设施层（MQ）负责传输它。
 */
@Data
public class RiskRequestMsg implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 交易流水号 (用于回调接力)
     */
    private String txnId;

    /**
     * 付款账号
     */
    private String accountNo;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 目标国家 (用于风控规则判断)
     */
    private String targetCountry;

    // 可以根据需要补充其他字段，如收款人信息、币种等
}