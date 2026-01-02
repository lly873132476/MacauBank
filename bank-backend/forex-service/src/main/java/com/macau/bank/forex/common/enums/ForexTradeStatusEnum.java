package com.macau.bank.forex.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 外汇交易订单状态
 */
@Getter
@AllArgsConstructor
public enum ForexTradeStatusEnum {

    PROCESSING(0, "处理中"),
    SUCCESS(1, "成功"),
    FAIL(2, "失败"),
    REFUNDED(3, "冲正/退款");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
