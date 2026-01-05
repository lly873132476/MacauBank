package com.macau.bank.account.domain.model;

import com.macau.bank.account.domain.entity.AccountBalance;
import com.macau.bank.account.domain.entity.AccountInfo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class UserAssetView {
    private final BigDecimal totalMopValue;
    private final List<AccountInfo> accounts;
    private final Map<String, List<AccountBalance>> balanceMap;

    public static UserAssetView empty() {
        return UserAssetView.builder()
                .totalMopValue(BigDecimal.ZERO)
                .accounts(new ArrayList<>())
                .balanceMap(new HashMap<>())
                .build();
    }
}
