package com.macau.bank.forex.domain.repository;

import com.macau.bank.forex.domain.entity.BankPosition;

public interface BankPositionRepository {
    BankPosition findByCurrency(String currencyCode);
    boolean update(BankPosition bankPosition);
}
