package com.macau.bank.account.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.AccountCategory;
import com.macau.bank.common.core.enums.AccountStatus;
import com.macau.bank.common.core.enums.AccountType;
import com.macau.bank.common.core.enums.RiskLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账户主档持久化对象
 */
@Getter
@Setter
@ToString
@TableName("account_info")
public class AccountInfoPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userNo;
    private String accountNo;
    private String cardNumber;
    private AccountCategory accountCategory;
    private AccountType accountType;
    private AccountStatus status;
    private RiskLevel riskLevel;
    private String openBranchCode;
    private String openBranchName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
