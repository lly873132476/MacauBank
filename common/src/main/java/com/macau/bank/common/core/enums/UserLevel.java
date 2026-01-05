package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户等级体系
 * 用于控制转账限额、手续费率、理财准入门槛等
 */
@Getter
@AllArgsConstructor
public enum UserLevel {

    NORMAL("NORMAL", "普通用户"),
    KEY("KEY", "U盾用户/安全加强用户"),
    VIP("VIP", "贵宾客户");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
