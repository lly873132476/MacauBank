package com.macau.bank.forex.domain.repository;

import com.macau.bank.forex.domain.entity.CurrencyPairConfig;

public interface CurrencyPairConfigRepository {
    CurrencyPairConfig findByPairCode(String pairCode);
}
