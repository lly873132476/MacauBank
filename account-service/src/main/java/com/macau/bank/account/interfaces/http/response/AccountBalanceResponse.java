package com.macau.bank.account.interfaces.http.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账户余额响应
 * <p>
 * 展示单一币种的资金状况，包括余额、可用额度及冻结金额
 * </p>
 */
@Data
@Schema(description = "账户余额明细")
public class AccountBalanceResponse implements Serializable {

    @Schema(description = "币种代码", example = "MOP")
    private String currencyCode;

    @Schema(description = "账面余额 (总资产)", example = "10050.00")
    private BigDecimal balance;

    @Schema(description = "可用余额 (可交易)", example = "10000.00")
    private BigDecimal availableBalance;

    @Schema(description = "冻结金额", example = "50.00")
    private BigDecimal frozenAmount;

    @Schema(description = "累计收入", example = "20000.00")
    private BigDecimal totalIncome;

    @Schema(description = "累计支出", example = "9950.00")
    private BigDecimal totalOutcome;

    @Schema(description = "最后更新时间", example = "2023-10-28 15:30:00")
    private String updateTime;
}
