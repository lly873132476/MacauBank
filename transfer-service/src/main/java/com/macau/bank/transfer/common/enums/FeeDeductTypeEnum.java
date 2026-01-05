package com.macau.bank.transfer.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 手续费扣费方式
 */
@Getter
@AllArgsConstructor
public enum FeeDeductTypeEnum {
    EXTRA(1, "外扣(额外收)"),
    INTERNAL(2, "内扣(从转账金额扣)");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
