package com.macau.bank.api.account.request;

import com.macau.bank.common.core.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建账户 RPC 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户编号 */
    private String userNo;

    /** 账户类型 */
    private AccountType accountType;

    /** 初始币种代码 */
    private String initialCurrencyCode;

    /** 初始余额 */
    private BigDecimal initialBalance;
}
