package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会计状态
 * 对应 AccountSubLedger.status
 */
@Getter
@AllArgsConstructor
public enum AccountingStatus {

    /** 正常入账 */
    NORMAL(1, "正常入账"),

    /** 红字冲正 (Reversal / Red Ink) */
    REVERSED(2, "红字冲正"),

    /** 蓝字补账 (Supplement / Blue Ink) */
    SUPPLEMENT(3, "蓝字补账");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;
    
}