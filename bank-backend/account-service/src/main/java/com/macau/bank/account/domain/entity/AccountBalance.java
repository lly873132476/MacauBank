package com.macau.bank.account.domain.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户余额领域实体
 */
@Getter
@Setter
@ToString
public class AccountBalance implements Serializable {
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
    private String lastFlowId;
    private String macCode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // Domain Logic: Check if balance is sufficient
    public boolean hasSufficientBalance(BigDecimal amount) {
        return availableBalance != null && availableBalance.compareTo(amount) >= 0;
    }
}