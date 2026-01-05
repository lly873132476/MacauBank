package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客户风险等级枚举 (AML反洗钱)
 */
@Getter
@AllArgsConstructor
public enum RiskLevel {

    /** 低风险 (Low Risk) - 默认 */
    LOW(1, "低风险"),

    /** 中风险 (Medium Risk) - 需关注 */
    MEDIUM(2, "中风险"),

    /** 高风险 (High Risk) - 需人工审核或拒绝业务 */
    HIGH(3, "高风险"),
    
    /** 黑名单 (Blacklist) - 禁止业务 */
    BLACKLIST(4, "黑名单");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}