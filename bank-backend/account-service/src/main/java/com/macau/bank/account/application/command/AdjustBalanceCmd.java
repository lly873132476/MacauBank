package com.macau.bank.account.application.command;

import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.framework.web.model.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 调整余额指令
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustBalanceCmd extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 账户号 */
    private String accountNo;

    /** 币种 */
    private String currencyCode;

    /** 变动金额 (正数增加，负数减少) */
    private BigDecimal amount;

    /** 描述/备注 */
    private String description;

    /** 业务流水号，全系统关联 */
    private String bizNo;

    /** 幂等请求ID (上游传递，用于防重复) */
    private String requestId;

    /** 业务类型 (可选，默认为空) */
    private BizType bizType;
}
