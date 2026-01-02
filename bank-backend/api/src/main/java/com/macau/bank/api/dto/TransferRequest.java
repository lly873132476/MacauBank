package com.macau.bank.api.dto;

import com.macau.bank.common.core.enums.FeeType;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 转账请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "转账提交表单")
public class TransferRequest extends BaseRequest {
    private static final long serialVersionUID = 1L;

    @Schema(description = "转出账号", example = "888800012345")
    @NotBlank(message = "转出账号不能为空")
    private String fromAccountNo;

    @Schema(description = "转入账号", example = "888800067890")
    @NotBlank(message = "转入账号不能为空")
    private String toAccountNo;

    @Schema(description = "转入账户姓名", example = "张三")
    @NotBlank(message = "转入账户姓名不能为空")
    private String toAccountName;

    @Schema(description = "转入银行代码", example = "BOC")
    private String toBankCode;

    @Schema(description = "转入银行 SWIFT 代码", example = "BKCHMOMXXXX")
    private String swiftCode;

    @Schema(description = "转入银行 FPS ID (转数快标识)", example = "12345678")
    private String fpsId;

    @Schema(description = "转账金额", example = "5000.00")
    @NotNull(message = "转账金额不能为空")
    @Positive(message = "转账金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "货币代码", example = "MOP")
    @NotBlank(message = "货币代码不能为空")
    private String currencyCode;

    @Schema(description = "手续费承担方式 (SHA=共同承担/OUR=转出方承担/BEN=转入方承担)", example = "SHA")
    private FeeType feeType;

    @Schema(description = "交易密码", example = "123456")
    @NotBlank(message = "交易密码不能为空")
    @ToString.Exclude // 安全关键：禁止打印日志
    private String transactionPassword;

    @Schema(description = "转账备注/附言", example = "购物款")
    private String remark;

    @Schema(description = "幂等性键 (防重复提交)", example = "TXN_20251231_001")
    @NotBlank(message = "幂等性键不能为空")
    private String idempotentKey;

    @Schema(description = "转账类型 (INTERNAL=行内/CROSS_BORDER=跨境/FPS=转数快)", example = "INTERNAL")
    @NotNull(message = "转账类型不能为空")
    private TransferType transferType;
}