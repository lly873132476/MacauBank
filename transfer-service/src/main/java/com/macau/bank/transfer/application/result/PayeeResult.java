package com.macau.bank.transfer.application.result;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收款人查询结果
 */
@Data
public class PayeeResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String payeeName;
    private String accountNo;
    private String bankCode;
    private String bankName;
    private String currencyCode;
    private String aliasName;
    private Integer totalTransCount;
    private LocalDateTime lastTransTime;
}
