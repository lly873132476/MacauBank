package com.macau.bank.transfer.application.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 转账记录Result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferOrderResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String txnId; // 交易流水号
    private Long payerAccountId; // 付款账户ID
    private String payeeAccountNo; // 收款账户号
    private BigDecimal amount; // 金额
    private String currencyCode; // 货币代码
    private BigDecimal fee; // 手续费
    private String status; // 状态
    private String transferType; // 转账类型
    private String transferChannel; // 转账通道
    private LocalDateTime createTime; // 创建时间
}