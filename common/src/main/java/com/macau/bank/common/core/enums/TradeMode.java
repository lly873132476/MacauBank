package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易模式枚举 (1-T+0, 2-T+1)
 */
@Getter
@AllArgsConstructor
public enum TradeMode {

    T_0(1, "T+0实时"),
    T_1(2, "T+1交割");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
