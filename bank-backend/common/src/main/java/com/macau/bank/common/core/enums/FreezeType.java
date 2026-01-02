package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 冻结类型枚举
 */
@Getter
@AllArgsConstructor
public enum FreezeType {

    TRANSACTION(1, "交易冻结"),
    JUDICIAL(2, "司法冻结"),
    ERROR_CORRECTION(3, "错账冻结");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
