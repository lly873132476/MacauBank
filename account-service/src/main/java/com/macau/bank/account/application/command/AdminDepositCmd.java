package com.macau.bank.account.application.command;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 管理员充值指令
 */
@Data
@Builder
public class AdminDepositCmd implements Serializable {
    /**
     * 幂等请求ID
     */
    private String requestId;

    /**
     * 用户编号
     */
    private String userNo;

    /**
     * 币种
     */
    private String currencyCode;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 管理员密钥 (可选，应用层可再校验一次)
     */
    private String adminSecret;
}
