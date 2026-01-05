package com.macau.bank.account.application.result;

import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.FlowDirection;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class TransactionFlowResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String txnId;
    private String currencyCode;
    private FlowDirection direction;
    private BigDecimal amount;
    private BigDecimal balance;
    private BizType bizType;
    private String bizDesc;
    private Map<String, Object> opponentInfo;
    private LocalDateTime transTime;
}
