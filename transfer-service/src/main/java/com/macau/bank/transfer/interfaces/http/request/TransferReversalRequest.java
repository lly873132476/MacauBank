package com.macau.bank.transfer.interfaces.http.request;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 转账冲正/退款请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "转账冲正请求")
public class TransferReversalRequest extends BaseRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", example = "12345")
    private Long orderId;

    @NotBlank(message = "冲正原因不能为空")
    @Schema(description = "冲正原因", example = "客户申请退款")
    private String reversalReason;
}
