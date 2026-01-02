package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AML 状态枚举
 */
@Getter
@AllArgsConstructor
public enum AmlStatus {

    PENDING("PENDING", "待审核"),
    PASSED("PASSED", "审核通过"),
    REJECTED("REJECTED", "审核拒绝"),
    MANUAL_REVIEW("MANUAL_REVIEW", "人工审核"),
    REPORTED("REPORTED", "已上报");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
