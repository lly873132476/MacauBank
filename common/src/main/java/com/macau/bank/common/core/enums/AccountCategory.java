package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户大类枚举
 */
@Getter
@AllArgsConstructor
public enum AccountCategory {

    /** 个人户 (Personal Banking) */
    PERSONAL(1, "个人户"),

    /** 企业户 (Corporate Banking) */
    CORPORATE(2, "企业户"),

    /** 内部户 (Corporate Banking) */
    Internal(3, "内部户");

    @EnumValue  // 存数据库
    @JsonValue  // 返给前端
    private final Integer code;
    private final String desc;
}