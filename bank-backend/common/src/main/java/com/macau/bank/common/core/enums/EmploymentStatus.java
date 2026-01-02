package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 就业状态枚举
 */
@Getter
@AllArgsConstructor
public enum EmploymentStatus {

    EMPLOYED(1, "受雇"),
    SELF_EMPLOYED(2, "自雇"),
    UNEMPLOYED(3, "待业"),
    RETIRED(4, "退休"),
    STUDENT(5, "学生");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
