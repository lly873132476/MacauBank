package com.macau.bank.api.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetSummaryRpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private BigDecimal totalMopValue;
    private List<AccountInfoRpcResponse> accounts;
}
