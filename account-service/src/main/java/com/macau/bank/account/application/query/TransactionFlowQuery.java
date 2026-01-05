package com.macau.bank.account.application.query;

import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.FlowDirection;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class TransactionFlowQuery implements Serializable {
    private String userNo;
    private String accountNo;
    private String currencyCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private FlowDirection direction;
    private BizType bizType;
    private Integer page;
    private Integer pageSize;
}
