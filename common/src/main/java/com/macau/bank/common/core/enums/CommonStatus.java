package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举 (1-启用/生效 0-禁用/失效)
 */
@Getter
@AllArgsConstructor
public enum CommonStatus {

    DISABLED(0, "停用/失效"),
    ENABLED(1, "启用/生效");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
