package com.macau.bank.forex.application.service.impl;

import com.macau.bank.common.core.enums.Currency;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.forex.application.service.CurrencyAppService;
import com.macau.bank.forex.domain.entity.ExchangeRate;
import com.macau.bank.forex.domain.service.CurrencyDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CurrencyAppServiceImpl implements CurrencyAppService {

    @Resource
    private CurrencyDomainService currencyDomainService;

    @Override
    public List<ExchangeRate> getReferenceRates() {
        log.info("应用服务 - 获取参考汇率列表");
        return currencyDomainService.getAllExchangeRates();
    }

    @Override
    public Map<String, BigDecimal> getBatchExchangeRates(List<String> fromCurrencies, String toCurrency) {
        log.info("应用服务 - 批量获取汇率: fromCurrencies={}, toCurrency={}", fromCurrencies, toCurrency);
        if (fromCurrencies == null || fromCurrencies.isEmpty()) {
            return new HashMap<>();
        }
        return fromCurrencies.stream()
                .distinct()
                .collect(Collectors.toMap(
                        from -> from,
                        from -> getExchangeRate(from, toCurrency),
                        (k1, k2) -> k1
                ));
    }

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        log.info("应用服务 - 获取汇率: fromCurrency={}, toCurrency={}", fromCurrency, toCurrency);
        return currencyDomainService.getExchangeRate(fromCurrency, toCurrency);
    }

    @Override
    public BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount) {
        log.info("应用服务 - 货币转换: from={}, to={}, amount={}", fromCurrency, toCurrency, amount);
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("应用服务 - 金额必须大于0: amount={}", amount);
            throw new BusinessException("金额必须大于0");
        }

        BigDecimal rate = currencyDomainService.getExchangeRate(fromCurrency, toCurrency);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal convertToMOP(String fromCurrency, BigDecimal amount) {
        log.info("应用服务 - 换算为MOP等值: fromCurrency={}, amount={}", fromCurrency, amount);
        
        if (Currency.MOP.getCode().equals(fromCurrency)) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
        
        return convert(fromCurrency, Currency.MOP.getCode(), amount);
    }
}