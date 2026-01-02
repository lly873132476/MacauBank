package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;

public enum FlowDirection {
    
    IN("C", "进账/余额增加"),
    OUT("D", "出账/余额减少");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    FlowDirection(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }
    
    // 🔥 核心：给你一个金额，自动判断是C还是D
    public static FlowDirection resolve(BigDecimal amount) {
        // 如果金额大于0，就是进账(C)；小于0，就是出账(D)
        return amount.compareTo(BigDecimal.ZERO) >= 0 ? IN : OUT;
    }
}