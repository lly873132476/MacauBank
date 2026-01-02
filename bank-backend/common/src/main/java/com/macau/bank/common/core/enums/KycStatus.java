package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * KycStatus枚举
 */
@Getter
@AllArgsConstructor
public enum KycStatus {

    /** 未认证状态 */
    UNVERIFIED(0, "未认证"),

    /** 审核中 */
    PENDING(1, "审核中"),

    /** 审核通过 */
    PASSED(2, "审核通过"),

    /** 审核驳回 */
    FAILED(3, "审核驳回");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
    
}