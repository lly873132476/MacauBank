package com.macau.bank.transfer.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预约计划状态
 */
@Getter
@AllArgsConstructor
public enum ScheduleStatusEnum {
    PAUSED(0, "暂停"),
    ACTIVE(1, "生效中"),
    FINISHED(2, "已结束");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
