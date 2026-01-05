package com.macau.bank.account.application.command;

import com.macau.bank.common.core.enums.BizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 扣款命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCmd {

    /** 账户号 */
    private String accountNo;

    /** 币种代码 */
    private String currencyCode;

    /** 扣款金额 (必须为正数) */
    private BigDecimal amount;

    /** 业务描述 */
    private String description;

    /** 业务流水号 */
    private String bizNo;

    /** 幂等请求ID */
    private String requestId;

    /** 业务类型 */
    private BizType bizType;
}
