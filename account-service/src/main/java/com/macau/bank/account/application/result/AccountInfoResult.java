package com.macau.bank.account.application.result;

import com.macau.bank.common.core.enums.AccountCategory;
import com.macau.bank.common.core.enums.AccountStatus;
import com.macau.bank.common.core.enums.AccountType;
import com.macau.bank.common.core.enums.RiskLevel;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userNo;
    private String accountNo;
    private String accountName;
    private String cardNumber;
    private AccountCategory accountCategory;
    private AccountType accountType;
    private AccountStatus status;
    private RiskLevel riskLevel;
    private String openBranchCode;
    private String openBranchName;
    private LocalDateTime createTime;

    /**
     * 账户下的币种余额列表
     */
    private List<AccountBalanceResult> balances;
}
