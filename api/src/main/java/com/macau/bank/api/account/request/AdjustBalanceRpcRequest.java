package com.macau.bank.api.account.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 调整余额 RPC 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustBalanceRpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 账户号 */
    private String accountNo;

    /** 币种代码 */
    private String currencyCode;

    /** 金额 (正数增加，负数减少) */
    private BigDecimal amount;

    /** 业务描述 */
    private String description;

    /** 业务流水号 */
    private String bizNo;

    /** 幂等请求ID */
    private String requestId;
}
