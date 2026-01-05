package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 转账通道枚举
 */
@Getter
@AllArgsConstructor
public enum TransferChannel {

    INTERNAL("INTERNAL", "行内清算"),
    SWIFT("SWIFT", "SWIFT"),
    FPS("FPS", "转数快"),
    CIPS("CIPS", "CIPS"),
    LOCAL_CLEARING("LOCAL_CLEARING", "本地清算");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
