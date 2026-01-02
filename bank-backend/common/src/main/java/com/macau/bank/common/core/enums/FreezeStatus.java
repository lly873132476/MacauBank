package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 冻结状态枚举
 */
@Getter
@AllArgsConstructor
public enum FreezeStatus {

    FROZEN(0, "已冻结"),
    UNFROZEN(1, "已解冻"),
    DEDUCTED(2, "已扣款");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
