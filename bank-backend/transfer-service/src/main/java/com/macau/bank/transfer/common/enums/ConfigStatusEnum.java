package com.macau.bank.transfer.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 转账配置类通用状态
 */
@Getter
@AllArgsConstructor
public enum ConfigStatusEnum {
    DISABLED(0, "停用/失效"),
    ENABLED(1, "启用/正常");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
