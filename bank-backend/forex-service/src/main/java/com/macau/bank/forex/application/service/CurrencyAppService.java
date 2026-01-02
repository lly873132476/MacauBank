package com.macau.bank.forex.application.service;

import com.macau.bank.forex.domain.entity.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CurrencyAppService {
    BigDecimal convertToMOP(String fromCurrency, BigDecimal amount);
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);
    BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount);
    Map<String, BigDecimal> getBatchExchangeRates(List<String> fromCurrencies, String toCurrency);
    
    /**
     * 获取参考汇率列表 (用于首页展示)
     */
    List<ExchangeRate> getReferenceRates();
}