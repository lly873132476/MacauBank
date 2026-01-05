package com.macau.bank.transfer.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 风控请求消息体
 * <p>
 * 属于领域层定义的“契约”，基础设施层（MQ）负责传输它。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskResultMsg implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 交易流水号 (用于回调接力)
     */
    private String txnId;

    /**
     * 风控结果
     */
    private boolean isPass;

    /**
     * 备注/拒绝原因
     */
    private String reason;

}