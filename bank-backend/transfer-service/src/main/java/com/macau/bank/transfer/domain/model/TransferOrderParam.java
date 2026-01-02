package com.macau.bank.transfer.domain.model;

import com.macau.bank.common.core.enums.TransferChannel;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.enums.TransferType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 转账订单创建参数 - 值对象
 * 严格对齐 DDD valobj 规范
 */
@Getter
@Builder
@ToString
public class TransferOrderParam {
    private final String idempotentKey;
    private final String userNo;
    private final Long payerAccountId;
    private final String payerAccountNo;
    private final String payerAccountName;
    private final String payerCurrency;
    private final String payeeAccountNo;
    private final BigDecimal amount;
    private final String currencyCode;
    private final BigDecimal fee;
    private final TransferType transferType;
    private final TransferChannel channel;
    private final TransferStatus status;
}
