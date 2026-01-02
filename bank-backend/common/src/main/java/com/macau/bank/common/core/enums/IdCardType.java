package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 证件类型枚举
 */
@Getter
@AllArgsConstructor
public enum IdCardType {

    ID_CARD(1, "身份证"),
    PASSPORT(2, "护照"),
    HK_MACAU_PASS(3, "港澳通行证");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
