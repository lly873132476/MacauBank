package com.macau.bank.account.interfaces.http.dto;

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
 * 管理员充值请求参数
 * <p>
 * 仅用于后台或测试环境进行资金注入
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理员后台充值请求")
public class AdminDepositRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "请求唯一ID (幂等键)", example = "DEP-202312250001")
    @NotBlank(message = "请求ID不能为空")
    private String requestId;

    @Schema(description = "用户编号", example = "U20231027001")
    @NotBlank(message = "用户编号不能为空")
    private String userNo;

    @Schema(description = "币种 (MOP, HKD, CNY, USD)", example = "MOP")
    @NotBlank(message = "币种不能为空")
    private String currencyCode;

    @Schema(description = "充值金额", example = "1000.00")
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "备注说明", example = "测试充值")
    private String remark;

    @Schema(description = "管理员密钥", example = "MacauBankAdmin888")
    @NotBlank(message = "管理员密钥不能为空")
    private String adminSecret;
}