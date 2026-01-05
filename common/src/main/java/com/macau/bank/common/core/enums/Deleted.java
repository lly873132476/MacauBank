package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户类型枚举
 * <p>
 * 0:未删除 1:已删除
 * </p>
 */
@Getter
@AllArgsConstructor
public enum Deleted {

    /** 0:未删除 */
    NO_DELETED(0, "未删除"),

    /** 1:已删除 */
    DELETED(1, "已删除");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
    
}