package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息分类枚举
 */
@Getter
@AllArgsConstructor
public enum MessageCategory {

    TRANSACTION(1, "动账通知"),
    SECURITY(2, "安全中心"),
    SYSTEM(3, "系统公告");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
