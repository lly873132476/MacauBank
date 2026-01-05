package com.macau.bank.forex.interfaces.http.response;

import com.macau.bank.forex.common.enums.ForexTradeStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 外币兑换响应
 * <p>
 * 返回交易结果、最终成交汇率及资金变动详情
 * </p>
 */
@Data
@Builder
@Schema(description = "外币兑换交易结果")
public class ForexExchangeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "交易流水号", example = "FX202310270001")
    private String txnId;

    @Schema(description = "成交汇率", example = "1.0315")
    private BigDecimal dealRate;

    @Schema(description = "卖出币种", example = "MOP")
    private String sellCurrency;

    @Schema(description = "卖出金额", example = "1031.50")
    private BigDecimal sellAmount;

    @Schema(description = "买入币种", example = "HKD")
    private String buyCurrency;

    @Schema(description = "买入金额", example = "1000.00")
    private BigDecimal buyAmount;

    @Schema(description = "交易状态 (1:成功, 2:失败)", example = "1")
    private ForexTradeStatusEnum status;

    @Schema(description = "交易完成时间", example = "2023-10-27 10:30:00")
    private String transTime;
}
