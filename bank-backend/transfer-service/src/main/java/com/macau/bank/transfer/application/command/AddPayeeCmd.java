package com.macau.bank.transfer.application.command;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加收款人命令
 */
@Data
public class AddPayeeCmd implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户编号
     */
    private String userNo;

    /**
     * 收款人姓名
     */
    private String payeeName;

    /**
     * 收款账号
     */
    private String accountNo;

    /**
     * 银行代码
     */
    private String bankCode;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 币种代码
     */
    private String currencyCode;

    /**
     * 别名
     */
    private String aliasName;
}
