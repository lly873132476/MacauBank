package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 风控状态枚举
 */
@Getter
@AllArgsConstructor
public enum RiskStatus {

    PENDING("PENDING", "待审核"),
    PASSED("PASSED", "审核通过"),
    REJECTED("REJECTED", "审核拒绝"),
    MANUAL_REVIEW("MANUAL_REVIEW", "人工审核");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
