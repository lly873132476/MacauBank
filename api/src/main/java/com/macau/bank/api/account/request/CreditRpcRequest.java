package com.macau.bank.api.account.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.macau.bank.common.core.enums.BizType;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 入账 RPC 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 账户号 */
    private String accountNo;

    /** 币种代码 */
    private String currencyCode;

    /** 入账金额 (必须为正数) */
    private BigDecimal amount;

    /** 业务描述 */
    private String description;

    /** 业务流水号 */
    private String bizNo;

    /** 幂等请求ID */
    private String requestId;

    /** 业务类型 */
    private BizType bizType;
}
