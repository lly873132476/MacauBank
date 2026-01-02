package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户鉴权信息状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserAuthStatus {

    /** 禁用 */
    DISABLED(0, "禁用"),

    /** 正常 */
    NORMAL(1, "正常"),

    /** 冻结 */
    FREEZE(2, "冻结");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
    
}