package com.macau.bank.account.domain.repository;

import com.macau.bank.account.domain.entity.AccountCurrencyConfig;

public interface AccountCurrencyConfigRepository {
    AccountCurrencyConfig findByCurrencyCode(String currencyCode);
}
