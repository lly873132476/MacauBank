package com.macau.bank.account.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户余额持久化对象
 */
@Getter
@Setter
@ToString
@TableName("account_balance")
public class AccountBalancePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String accountNo;
    private String currencyCode;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalOutcome;
    
    @Version
    private Integer version;

    private String lastFlowId;
    private String macCode;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
