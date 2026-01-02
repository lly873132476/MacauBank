package com.macau.bank.forex.domain.service;

import com.macau.bank.common.core.enums.Currency;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.util.RedisUtil;
import com.macau.bank.forex.domain.entity.ExchangeRate;
import com.macau.bank.forex.domain.repository.ExchangeRateRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
public class CurrencyDomainService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ExchangeRateRepository exchangeRateRepository;

    private static final String RATE_CACHE_PREFIX = "exchange_rate:";
    private static final int RATE_CACHE_EXPIRE = 300; // 汇率缓存5分钟

    public List<ExchangeRate> getAllExchangeRates() {
        return exchangeRateRepository.findAllActive();
    }

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        // 1. 校验货币代码
        if (!Currency.isSupported(fromCurrency) || !Currency.isSupported(toCurrency)) {
            log.warn("领域服务 - 不支持的货币类型: from={}, to={}", fromCurrency, toCurrency);
            throw new BusinessException("不支持的货币类型");
        }

        // 2. 相同货币返回1
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        String directCurrencyPair = fromCurrency + "_" + toCurrency;
        String reverseCurrencyPair = toCurrency + "_" + fromCurrency;

        // 3. 从Redis缓存获取汇率
        String cacheKey = RATE_CACHE_PREFIX + directCurrencyPair;
        String cachedRate = redisUtil.get(cacheKey);
        if (cachedRate != null) {
            return new BigDecimal(cachedRate);
        }

        // 4. 从数据库获取汇率
        ExchangeRate exchangeRate = getRateByPair(directCurrencyPair);
        BigDecimal rateToReturn = null;

        if (exchangeRate != null) {
            // 如果查询到的货币对是 fromCurrency_toCurrency
            if (fromCurrency.equals(exchangeRate.getBaseCurrency()) && toCurrency.equals(exchangeRate.getTargetCurrency())) {
                // 用户卖出基准货币 (fromCurrency)，银行买入基准货币，使用 Bank Buy Rate
                rateToReturn = exchangeRate.getBankBuyRate().divide(new BigDecimal(exchangeRate.getUnit()), 8, RoundingMode.HALF_UP);
            } else if (fromCurrency.equals(exchangeRate.getTargetCurrency()) && toCurrency.equals(exchangeRate.getBaseCurrency())) {
                // 用户卖出目标货币 (fromCurrency)，银行卖出基准货币，使用 Bank Sell Rate 的倒数
                rateToReturn = BigDecimal.ONE.divide(exchangeRate.getBankSellRate().divide(new BigDecimal(exchangeRate.getUnit()), 8, RoundingMode.HALF_UP), 8, RoundingMode.HALF_UP);
            }
        } else {
            // 尝试反向货币对
            exchangeRate = getRateByPair(reverseCurrencyPair);
            if (exchangeRate != null) {
                // 如果查询到的是 toCurrency_fromCurrency
                if (toCurrency.equals(exchangeRate.getBaseCurrency()) && fromCurrency.equals(exchangeRate.getTargetCurrency())) {
                    // 用户卖出 fromCurrency (target), 银行卖出基准货币(toCurrency)，使用 Bank Sell Rate 的倒数
                    rateToReturn = BigDecimal.ONE.divide(exchangeRate.getBankSellRate().divide(new BigDecimal(exchangeRate.getUnit()), 8, RoundingMode.HALF_UP), 8, RoundingMode.HALF_UP);
                } else if (toCurrency.equals(exchangeRate.getTargetCurrency()) && fromCurrency.equals(exchangeRate.getBaseCurrency())) {
                    // 用户卖出 fromCurrency (base), 银行买入基准货币(fromCurrency)，使用 Bank Buy Rate
                    rateToReturn = exchangeRate.getBankBuyRate().divide(new BigDecimal(exchangeRate.getUnit()), 8, RoundingMode.HALF_UP);
                }
            }
        }

        if (rateToReturn == null || rateToReturn.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("领域服务 - 汇率数据不存在或无效: from={}, to={}", fromCurrency, toCurrency);
            throw new BusinessException("汇率数据不存在或无效");
        }

        // 5. 缓存汇率
        redisUtil.setex(cacheKey, RATE_CACHE_EXPIRE, rateToReturn.toPlainString());

        return rateToReturn;
    }

    private ExchangeRate getRateByPair(String pair) {
        return exchangeRateRepository.findByPairCode(pair);
    }
}
