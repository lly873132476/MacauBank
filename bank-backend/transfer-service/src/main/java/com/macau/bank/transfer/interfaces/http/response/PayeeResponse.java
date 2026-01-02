package com.macau.bank.transfer.interfaces.http.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 收款人响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收款人响应信息")
public class PayeeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID", example = "1001")
    private Long id;

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

    @Schema(description = "别名", example = "房东")
    private String aliasName;

    @Schema(description = "最后转账时间", example = "2023-12-01 10:30:00")
    private String lastTransTime;

    @Schema(description = "累计转账次数", example = "15")
    private Integer totalTransCount;
}
