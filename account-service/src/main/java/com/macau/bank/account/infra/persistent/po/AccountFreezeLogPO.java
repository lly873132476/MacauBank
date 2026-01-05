package com.macau.bank.account.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.FreezeStatus;
import com.macau.bank.common.core.enums.FreezeType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金冻结记录持久化对象
 */
@Getter
@Setter
@ToString
@TableName("account_freeze_log")
public class AccountFreezeLogPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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
