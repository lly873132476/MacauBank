package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 转账类型枚举
 */
@Getter
@AllArgsConstructor
public enum TransferType {

    /**
     * 同行转账 (On-Us)
     * 特点：实时、0手续费、无需走外部清算网关。
     * 对应策略：InternalTransferStrategy
     */
    INTERNAL("INTERNAL", "同行转账"),

    /**
     * 本地跨行 (Off-Us Local)
     * 特点：走当地央行清算系统 (如香港FPS/澳门RTGS)。
     * 对应策略：LocalInterbankStrategy (未来扩展)
     */
    LOCAL("LOCAL", "本地跨行"),

    /**
     * 跨境汇款 (Off-Us International)
     * 特点：走 SWIFT/CIPS，T+N 到账，涉及汇率、反洗钱。
     * 对应策略：CrossBorderTransferStrategy
     */
    CROSS_BORDER("CROSS_BORDER", "跨境汇款");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
    
}