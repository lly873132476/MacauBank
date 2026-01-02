package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户状态枚举
 */
@Getter
@AllArgsConstructor
public enum AccountStatus {

    /** 正常 */
    NORMAL(1, "正常"),

    /** 冻结-只进不出 (Debit Frozen / Stop Payment) */
    FREEZE_DEBIT(2, "冻结(只进不出)"),

    /** 全封锁-不进不出 (Full Frozen) */
    FREEZE_FULL(3, "全封锁(不进不出)"),

    /** 注销 (Closed) */
    CLOSED(4, "注销");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}