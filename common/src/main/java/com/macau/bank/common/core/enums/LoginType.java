package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型枚举
 */
@Getter
@AllArgsConstructor
public enum LoginType {

    /** 手机验证码登录 */
    SMS("SMS", "手机验证码登录"),

    /** 密码登录 */
    PASSWORD("PASSWORD", "密码登录");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static LoginType getByCode(String code) {
        for (LoginType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
