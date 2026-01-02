package com.macau.bank.transfer.interfaces.http.request;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 转账预校验请求DTO
 * <p>
 * 在正式发起转账前，校验额度、账户状态并计算手续费
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "转账预校验请求")
public class VerifyRequest extends BaseRequest {
    private static final long serialVersionUID = 1L;

    @Schema(description = "付款账户ID", example = "1001")
    @NotNull(message = "付款账户ID不能为空")
    private Long fromAccountId;

    @Schema(description = "收款账户号", example = "888800012345")
    @NotBlank(message = "收款账号不能为空")
    private String toAccount;

    @Schema(description = "转账金额", example = "5000.00")
    @NotNull(message = "转账金额不能为空")
    @Positive(message = "转账金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "币种代码", example = "MOP")
    @NotBlank(message = "币种代码不能为空")
    private String currencyCode;

    @Schema(description = "转账类型 (NORMAL, CROSS_BORDER, FPS)", example = "NORMAL")
    @NotBlank(message = "转账类型不能为空")
    private String transferType;

    @Schema(description = "转账附言/备注", example = "购物")
    private String remark;
}