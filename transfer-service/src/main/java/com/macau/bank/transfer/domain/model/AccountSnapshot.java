package com.macau.bank.transfer.domain.model;

import com.macau.bank.api.account.response.AccountBalanceRpcResponse;
import com.macau.bank.common.core.enums.AccountCategory;
import com.macau.bank.common.core.enums.AccountStatus;
import com.macau.bank.common.core.enums.AccountType;
import com.macau.bank.common.core.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户信息 RPC 响应
 * <p>
 * 用于跨服务传输账户主档及余额信息
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;

    private String userNo;

    private String accountNo;

    private String accountName;

    private String currencyCode;

    private String cardNumber;

    private AccountCategory accountCategory;

    private AccountType accountType;

    private AccountStatus status;

    private RiskLevel riskLevel;

    private String openBranchCode;

    private String openBranchName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<AccountBalanceRpcResponse> balances;
}
