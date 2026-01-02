package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信发送状态枚举
 */
@Getter
@AllArgsConstructor
public enum SmsStatus {

    SENDING(0, "发送中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
