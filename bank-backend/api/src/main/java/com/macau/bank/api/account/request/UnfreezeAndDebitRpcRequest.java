package com.macau.bank.api.account.request;

import com.macau.bank.common.core.enums.BizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 解冻并扣款 RPC 请求
 * <p>
 * 场景：转账冻结 → 确认成功 → 解冻并扣款
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnfreezeAndDebitRpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 账户号 */
    private String accountNo;

    /** 币种代码 */
    private String currencyCode;

    /** 扣款金额（正数） */
    private BigDecimal amount;

    /** 冻结流水号（用于关联冻结记录） */
    private String flowNo;

    /** 扣款原因/描述 */
    private String reason;

    /** 业务类型（由上游传递，如 TRANSFER_OUT） */
    private BizType bizType;

    /** 请求唯一标识（用于幂等性校验，需保证唯一） */
    private String requestId;
}
