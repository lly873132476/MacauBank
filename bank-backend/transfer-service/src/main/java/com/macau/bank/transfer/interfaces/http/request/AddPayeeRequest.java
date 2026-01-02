package com.macau.bank.transfer.interfaces.http.request;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 添加收款人请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "添加收款人请求")
public class AddPayeeRequest extends BaseRequest {
    private static final long serialVersionUID = 1L;

    @Schema(description = "收款人姓名", example = "张三")
    @NotBlank(message = "收款人姓名不能为空")
    private String payeeName;

    @Schema(description = "收款账号", example = "888800012345")
    @NotBlank(message = "收款账号不能为空")
    private String accountNo;

    @Schema(description = "银行代码", example = "BOC")
    @NotBlank(message = "银行代码不能为空")
    private String bankCode;

    @Schema(description = "银行名称", example = "中国银行")
    @NotBlank(message = "银行名称不能为空")
    private String bankName;

    @Schema(description = "币种代码", example = "MOP")
    private String currencyCode;

    @Schema(description = "别名", example = "房东")
    private String aliasName;
}
