package com.macau.bank.transfer.interfaces.http.response;

import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.enums.TransferType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "转账交易响应")
public class TransferResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "内部交易ID (主键)", example = "100001")
    private Long transactionId;

    @Schema(description = "付款账号", example = "1001")
    private Long fromAccountNo;

    @Schema(description = "收款账号", example = "6222021234567890")
    private String toAccountNo;

    @Schema(description = "交易金额", example = "5000.00")
    private BigDecimal amount;

    @Schema(description = "币种", example = "MOP")
    private String currencyCode;

    @Schema(description = "实收手续费", example = "5.00")
    private BigDecimal fee;

    @Schema(description = "交易状态", example = "SUCCESS")
    private TransferStatus status;

    @Schema(description = "转账类型", example = "NORMAL")
    private TransferType transferType;

    @Schema(description = "交易流水号 (全局唯一)", example = "T2023102700001")
    private String txnId;

    @Schema(description = "交易创建时间", example = "2023-10-27 10:05:00")
    private String createTime;
}
