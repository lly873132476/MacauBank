package com.macau.bank.account.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.macau.bank.common.core.enums.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 账户分户账明细持久化对象
 */
@Getter
@Setter
@ToString
@TableName(value = "account_sub_ledger", autoResultMap = true)
public class AccountSubLedgerPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String voucherNo;
    private String bizNo;
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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> opponentInfo;

    private LocalDate acctDate;
    private LocalDateTime transTime;
    private LocalDateTime reconcileTime;
}
