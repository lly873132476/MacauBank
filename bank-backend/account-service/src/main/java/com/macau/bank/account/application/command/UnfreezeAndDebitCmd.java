package com.macau.bank.account.application.command;

import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.framework.web.model.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 解冻并扣款指令
 * <p>
 * 场景：转账冻结 → 确认成功 → 解冻并扣款
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UnfreezeAndDebitCmd extends BaseRequest {
    private static final long serialVersionUID = 1L;

    /** 账户号 */
    private String accountNo;

    /** 币种 */
    private String currencyCode;

    /** 扣款金额（正数） */
    private BigDecimal amount;

    /** 冻结流水号（用于关联冻结记录） */
    private String flowNo;

    /** 扣款原因/描述 */
    private String reason;

    /** 业务类型（由上游传递，如 TRANSFER_OUT） */
    private BizType bizType;

    /** 请求唯一标识（用于幂等性校验） */
    private String requestId;
}
