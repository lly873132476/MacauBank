package com.macau.bank.forex.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 汇率状态
 */
@Getter
@AllArgsConstructor
public enum RateStatusEnum {

    EXPIRED(0, "失效/历史"),
    EFFECTIVE(1, "生效/当前");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
