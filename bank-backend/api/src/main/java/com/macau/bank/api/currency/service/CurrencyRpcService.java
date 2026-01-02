package com.macau.bank.api.currency.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 货币汇率服务Dubbo接口
 */
public interface CurrencyRpcService {

    /**
     * 获取汇率
     */
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);

    /**
     * 货币转换
     */
    BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount);

    /**
     * 换算为MOP等值
     */
    BigDecimal convertToMOP(String fromCurrency, BigDecimal amount);

    /**
     * 批量获取币种相对于目标币种的汇率
     * @param fromCurrencies 来源币种列表
     * @param toCurrency 目标币种
     * @return 汇率 Map (币种 -> 汇率)
     */
    Map<String, BigDecimal> getBatchExchangeRates(List<String> fromCurrencies, String toCurrency);
}
