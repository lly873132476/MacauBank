package com.macau.bank.account.application.result;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountBalanceResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String accountNo;
    private String currencyCode;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalOutcome;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
