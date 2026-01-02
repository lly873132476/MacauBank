package com.macau.bank.account.application.command;

import com.macau.bank.common.core.enums.AccountType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建账户指令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountCmd implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户编号 */
    private String userNo;

    /** 账户类型 */
    private AccountType accountType;

    /** 初始币种 */
    private String initialCurrencyCode;

    /** 初始余额 */
    private BigDecimal initialBalance;
}
