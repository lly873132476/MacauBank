package com.macau.bank.forex.application.command;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class ExchangeCmd implements Serializable {
    /** 幂等请求ID */
    private String requestId;
    private String userNo;
    private String pairCode;
    private String sellCurrency;
    private BigDecimal sellAmount;
    private String buyCurrency;
    private String accountNo;
}
