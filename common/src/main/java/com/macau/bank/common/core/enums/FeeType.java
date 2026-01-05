package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 费用分摊方式枚举
 */
@Getter
@AllArgsConstructor
public enum FeeType {

    SHA("SHA", "共同承担"),
    OUR("OUR", "付款方承担"),
    BEN("BEN", "收款方承担");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
