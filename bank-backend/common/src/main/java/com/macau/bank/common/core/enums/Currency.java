package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 币种枚举 (ISO 4217标准)
 */
@Getter
@AllArgsConstructor
public enum Currency {

    /** 澳门元 (Macau Pataca) */
    MOP("MOP", "澳门元"),

    /** 港币 (Hong Kong Dollar) */
    HKD("HKD", "港币"),

    /** 人民币 (Chinese Yuan) */
    CNY("CNY", "人民币"),

    /** 美元 (US Dollar) */
    USD("USD", "美元");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    public static boolean isSupported(String fromCurrency) {
        for (Currency currency : Currency.values()) {

            if (currency.getCode().equals(fromCurrency)) {
                return true;
            }
        }
        return false;
    }
}