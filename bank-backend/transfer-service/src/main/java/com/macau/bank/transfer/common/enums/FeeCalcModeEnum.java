package com.macau.bank.transfer.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 手续费计费模式
 */
@Getter
@AllArgsConstructor
public enum FeeCalcModeEnum {
    FIXED(1, "固定金额"),
    PERCENTAGE(2, "百分比"),
    BOTH(3, "固定+百分比");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
