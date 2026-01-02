package com.macau.bank.forex.interfaces.http.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 首页实时汇率参考响应
 * <p>
 * 用于首页左右滚动的汇率看板，包含涨跌幅模拟数据
 * </p>
 */
@Data
@Builder
@Schema(description = "实时汇率参考信息")
public class ExchangeRateReferenceResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "货币对代码", example = "HKD/MOP")
    private String currencyPair;

    @Schema(description = "基准货币", example = "HKD")
    private String baseCurrency;

    @Schema(description = "目标货币", example = "MOP")
    private String targetCurrency;

    @Schema(description = "银行买入价", example = "1.0315")
    private BigDecimal buyRate;

    @Schema(description = "银行卖出价", example = "1.0295")
    private BigDecimal sellRate;

    @Schema(description = "24H涨跌幅 (%)", example = "+0.05")
    private String changePercent;

    @Schema(description = "更新时间", example = "10:30")
    private String updateTime;
}
