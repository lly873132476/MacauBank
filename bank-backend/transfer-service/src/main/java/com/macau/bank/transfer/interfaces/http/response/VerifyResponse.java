package com.macau.bank.transfer.interfaces.http.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 转账预校验响应DTO
 * <p>
 * 返回预估手续费、到账时间及合规检查结果
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "转账预校验结果")
public class VerifyResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "预估手续费", example = "5.00")
    private BigDecimal fee;

    @Schema(description = "预计到账时间", example = "实时")
    private String estimatedTime;

    @Schema(description = "是否需要补充证明文件 (反洗钱规则)", example = "false")
    private Boolean requireDocuments;

    @Schema(description = "校验提示信息", example = "校验通过")
    private String message;
}