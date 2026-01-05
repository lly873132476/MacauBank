package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * KycLevel枚举
 */
@Getter
@AllArgsConstructor
public enum KycLevel {

    /** 未认证 额度 0 */
    ANONYMOUS(0, "未认证"),

    /** 初级认证 额度 5,000 */
    BASIC(1, "初级认证"),

    /** 审核通过 额度 500,000 */
    PREMIUM(2, "高级认证");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
    
}