package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对账状态枚举
 * 对应 AccountSubLedger.checkStatus
 */
@Getter
@AllArgsConstructor
public enum CheckStatus {

    /** 未对账 (初始状态) */
    UNCHECKED(0, "未对账"),

    /** 对账平 (两边账目一致) */
    BALANCED(1, "对账平"),

    /** 对账不平 (出现差异) */
    UNBALANCED(2, "对账不平");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

}