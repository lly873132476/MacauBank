package com.macau.bank.transfer.application.command;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新收款人命令
 */
@Data
public class UpdatePayeeCmd implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userNo;
    private String payeeName;
    private String accountNo;
    private String bankCode;
    private String bankName;
    private String currencyCode;
    private String aliasName;
}
