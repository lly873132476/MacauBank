package com.macau.bank.account.interfaces.http.request;

import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.FlowDirection;
import com.macau.bank.common.framework.web.model.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 交易流水查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "交易流水查询请求参数")
public class TransactionFlowRequest extends BasePageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "银行账号", example = "888800012345")
    private String accountNo;

    @Schema(description = "币种代码", example = "MOP")
    private String currencyCode;

    @Schema(description = "开始日期 (YYYY-MM-DD)", example = "2023-10-01")
    private LocalDate startDate;

    @Schema(description = "结束日期 (YYYY-MM-DD)", example = "2023-10-31")
    private LocalDate endDate;

    @Schema(description = "借贷标志 (D:支出, C:收入)", example = "C")
    private FlowDirection direction;

    @Schema(description = "业务类型", example = "TRANSFER_IN")
    private BizType bizType;
}
