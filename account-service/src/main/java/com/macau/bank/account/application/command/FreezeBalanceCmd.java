package com.macau.bank.account.application.command;

import com.macau.bank.common.core.enums.FreezeType;
import com.macau.bank.common.framework.web.model.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 冻结余额指令
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FreezeBalanceCmd extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 账户号 */
    private String accountNo;

    /** 币种 */
    private String currencyCode;

    /** 冻结金额 */
    private BigDecimal amount;

    /** 业务流水号 */
    private String flowNo;

    /** 冻结类型 */
    private FreezeType freezeType;

    /** 冻结原因 */
    private String reason;
}
