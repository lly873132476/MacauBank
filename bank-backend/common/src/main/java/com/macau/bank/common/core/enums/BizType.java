package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务类型枚举
 * 对应 AccountSubLedger.bizType
 */
@Getter
@AllArgsConstructor
public enum BizType {

    /** 充值/存款 */
    DEPOSIT("DEPOSIT", "充值"),

    /** 提现/取款 (补全) */
    WITHDRAWAL("WITHDRAWAL", "提现"),

    /** 转账转出 */
    TRANSFER_OUT("TRANSFER_OUT", "转出"),

    /** 转账转入 (补全) */
    TRANSFER_IN("TRANSFER_IN", "转入"),

    /** 外汇兑换 */
    FOREX_EXCHANGE("FOREX_EXCHANGE", "外汇兑换"),

    /** 外汇点差利润 (用于内部核算) */
    FOREX_PROFIT("FOREX_PROFIT", "外汇点差利润"),

    /** 手续费 */
    FEE("FEE", "手续费"),

    /** 利息 */
    INTEREST("INTEREST", "利息"),
    
    /** 红字冲正 (配合 AccountingStatus 使用) */
    REVERSAL("REVERSAL", "冲正");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    // 根据字符串 code 获取枚举
    public static BizType getByCode(String code) {
        for (BizType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}