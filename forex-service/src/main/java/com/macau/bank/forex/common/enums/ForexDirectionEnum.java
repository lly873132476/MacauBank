package com.macau.bank.forex.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易方向
 */
@Getter
@AllArgsConstructor
public enum ForexDirectionEnum {

    BUY("BUY", "客户买入/银行卖出"),
    SELL("SELL", "客户卖出/银行买入");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
