package com.macau.bank.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易状态枚举
 */
@Getter
@AllArgsConstructor
public enum TransferStatus {

    INIT("INIT", "订单初始化"),
    PENDING_RISK("PENDING_RISK", "风控审核中"),
    SUCCESS("SUCCESS", "交易成功"),
    FAILED("FAILED", "交易失败"),
    PENDING_COMPENSATION("PENDING_COMPENSATION", "异常挂起，需人工审核");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
