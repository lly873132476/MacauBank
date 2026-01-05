package com.macau.bank.account.application.command;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 开通币种账户指令
 */
@Getter
@Setter
@ToString
public class AdminOpenCurrencyAccountCmd implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String accountNo;

    /**
     * 币种代码
     */
    private String currencyCode;

    /**
     * 管理员密钥 (可选，应用层可再校验一次)
     */
    private String adminSecret;
}
