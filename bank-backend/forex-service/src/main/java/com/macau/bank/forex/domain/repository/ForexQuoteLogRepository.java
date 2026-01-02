package com.macau.bank.forex.domain.repository;

import com.macau.bank.forex.domain.entity.ForexQuoteLog;

public interface ForexQuoteLogRepository {
    void save(ForexQuoteLog log);
}
