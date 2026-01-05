package com.macau.bank.api.account.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 解冻余额 RPC 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnfreezeBalanceRpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 账户号 */
    private String accountNo;

    /** 币种代码 */
    private String currencyCode;

    /** 解冻金额 */
    private BigDecimal amount;

    /** 业务流水号 */
    private String flowNo;

    /** 解冻原因 */
    private String reason;
}
