package com.macau.bank.forex.interfaces.rpc;

import com.macau.bank.api.currency.service.CurrencyRpcService;
import com.macau.bank.forex.application.service.CurrencyAppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.math.BigDecimal;

/**
 * 货币汇率RPC服务实现
 * 仅负责RPC接口暴露
 */
@Slf4j
@DubboService
public class CurrencyRpcServiceImpl implements CurrencyRpcService {

    @Resource
    private CurrencyAppService currencyAppService;

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        return currencyAppService.getExchangeRate(fromCurrency, toCurrency);
    }

    @Override
    public BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount) {
        return currencyAppService.convert(fromCurrency, toCurrency, amount);
    }

    @Override
    public BigDecimal convertToMOP(String fromCurrency, BigDecimal amount) {
        return currencyAppService.convertToMOP(fromCurrency, amount);
    }

    @Override
    public java.util.Map<String, BigDecimal> getBatchExchangeRates(java.util.List<String> fromCurrencies, String toCurrency) {
        return currencyAppService.getBatchExchangeRates(fromCurrencies, toCurrency);
    }
}