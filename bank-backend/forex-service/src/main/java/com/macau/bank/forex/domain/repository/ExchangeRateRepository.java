package com.macau.bank.forex.domain.repository;

import com.macau.bank.forex.domain.entity.ExchangeRate;
import java.util.List;

public interface ExchangeRateRepository {
    List<ExchangeRate> findAllActive();
    ExchangeRate findByPairCode(String pairCode);
}
