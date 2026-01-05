package com.macau.bank.transfer.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预约计划类型
 */
@Getter
@AllArgsConstructor
public enum ScheduleTypeEnum {
    SINGLE(1, "单次预约"),
    STANDING_ORDER(2, "周期性转账");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
