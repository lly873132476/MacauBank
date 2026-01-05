package com.macau.bank.account.domain.entity;

import com.macau.bank.common.core.enums.FreezeStatus;
import com.macau.bank.common.core.enums.FreezeType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金冻结记录领域对象
 */
@Getter
@Setter
@ToString
public class AccountFreezeLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String flowNo;
    private String accountNo;
    private String currencyCode;
    private BigDecimal amount;
    private FreezeType freezeType;
    private String reason;
    private FreezeStatus status;
    private LocalDateTime createTime;
    private LocalDateTime unfreezeTime;
}
