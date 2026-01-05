package com.macau.bank.forex.interfaces.http.request;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 外币兑换请求
 * <p>
 * 用户通过此接口发起实时外汇交易，系统将根据当前汇率执行本外币转换
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "外币兑换请求参数")
public class ForexExchangeRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "请求唯一ID (幂等键)", example = "REQ-202312250001")
    @NotBlank(message = "请求ID不能为空")
    private String requestId;

    @Schema(description = "交易对代码 (如 HKD_MOP)", example = "HKD_MOP")
    @NotBlank(message = "交易对不能为空")
    private String pairCode;

    @Schema(description = "卖出币种", example = "MOP")
    @NotBlank(message = "卖出币种不能为空")
    private String sellCurrency;

    @Schema(description = "卖出金额", example = "1000.00")
    @NotNull(message = "卖出金额不能为空")
    @DecimalMin(value = "0.01", message = "卖出金额必须大于0")
    private BigDecimal sellAmount;

    @Schema(description = "买入币种", example = "HKD")
    @NotBlank(message = "买入币种不能为空")
    private String buyCurrency;

    @Schema(description = "付款账户号", example = "888800012345")
    @NotBlank(message = "付款账户不能为空")
    private String accountNo;
}
