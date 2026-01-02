package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收款人类型枚举
 */
@Getter
@AllArgsConstructor
public enum PayeeType {

    HISTORY(0, "历史记录"),
    FREQUENT(1, "常用联系人");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
