package com.macau.bank.account.interfaces.http.response;

import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.FlowDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 交易流水响应
 */
@Data
@Schema(description = "账户交易流水明细")
public class TransactionFlowResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "流水ID", example = "10001")
    private Long id;

    @Schema(description = "交易流水号", example = "T20231027001")
    private String txnId;

    @Schema(description = "币种", example = "MOP")
    private String currencyCode;

    @Schema(description = "借贷标志 (D:支出, C:收入)", example = "C")
    private FlowDirection direction;

    @Schema(description = "交易金额", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "变动后余额", example = "10500.00")
    private BigDecimal balance;

    @Schema(description = "业务类型", example = "DEPOSIT")
    private BizType bizType;

    @Schema(description = "交易描述", example = "ATM存款")
    private String bizDesc;

    @Schema(description = "对手方信息 (JSON)", example = "{\"name\": \"ATM-001\"}")
    private Map<String, Object> opponentInfo;

    @Schema(description = "交易时间", example = "2023-10-27 10:30:00")
    private String transTime;
}
