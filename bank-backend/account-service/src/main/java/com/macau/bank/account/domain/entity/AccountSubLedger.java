package com.macau.bank.account.domain.entity;

import com.macau.bank.common.core.enums.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 账户分户账明细领域对象
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSubLedger implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String voucherNo;
    private String biz_no;
    private String requestId;
    private String userNo;
    private String accountNo;
    private String currencyCode;
    private FlowDirection cdFlag;
    private BigDecimal amount;
    private BigDecimal balance;
    private AccountingStatus status;
    private CheckStatus checkStatus;
    private SettleStatus settleStatus;
    private BizType bizType;
    private String bizDesc;
    private Map<String, Object> opponentInfo;
    private LocalDate acctDate;
    private LocalDateTime transTime;
    private LocalDateTime reconcileTime;
}
