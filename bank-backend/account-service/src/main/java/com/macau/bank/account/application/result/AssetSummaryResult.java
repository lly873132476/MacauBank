package com.macau.bank.account.application.result;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetSummaryResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal totalMopValue;
    private List<AccountInfoResult> accounts;
}
