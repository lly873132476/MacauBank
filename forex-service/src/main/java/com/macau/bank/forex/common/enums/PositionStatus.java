package com.macau.bank.forex.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 银行头寸状态
 */
@Getter
@AllArgsConstructor
public enum PositionStatus {
    
    STOPPED(0, "暂停交易(熔断)"),
    NORMAL(1, "正常");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
