package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户类型枚举
 * <p>
 * 1-储蓄账户, 2-往来账户(支票), 3-定期账户
 * </p>
 */
@Getter
@AllArgsConstructor
public enum AccountType {

    /** 储蓄账户 (Savings Account) - 最常用的活期存款 */
    SAVINGS(1, "储蓄账户"),

    /** 往来账户 (Current Account) - 通常用于支票往来 */
    CURRENT(2, "往来账户"),

    /** 定期账户 (Fixed Deposit) */
    FIXED(3, "定期账户"),
    
    /** 贷款账户 (Loan Account) */
    LOAN(4, "贷款账户");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
    
}