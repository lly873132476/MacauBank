package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否枚举 (1-是 0-否)
 */
@Getter
@AllArgsConstructor
public enum YesNo {

    NO(0, "否"),
    YES(1, "是");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
