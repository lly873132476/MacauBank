package com.macau.bank.transfer.interfaces.http.request;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新收款人请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "更新收款人请求")
public class UpdatePayeeRequest extends BaseRequest {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID", example = "1")
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "别名", example = "新别名")
    private String aliasName;

    @Schema(description = "收款人姓名", example = "张三")
    private String payeeName;

    @Schema(description = "收款账号", example = "888800012345")
    private String accountNo;

    @Schema(description = "银行代码", example = "BOC")
    private String bankCode;

    @Schema(description = "银行名称", example = "中国银行")
    private String bankName;

    @Schema(description = "币种代码", example = "MOP")
    private String currencyCode;
}
