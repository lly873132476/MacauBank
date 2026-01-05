package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结算状态枚举
 * 对应 AccountSubLedger.settleStatus
 */
@Getter
@AllArgsConstructor
public enum SettleStatus {

    /** 实时入账 (Real-time Settlement) */
    REALTIME(1, "实时入账"),

    /** 日终批处理 (Batch Processing) */
    BATCH(2, "日终批处理"),

    /** 挂账 (Suspended / On Hold) - 需人工介入或等后续指令 */
    SUSPENDED(3, "挂账");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

}