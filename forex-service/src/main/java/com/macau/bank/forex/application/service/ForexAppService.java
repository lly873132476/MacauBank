package com.macau.bank.forex.application.service;

import com.macau.bank.forex.application.command.ExchangeCmd;
import com.macau.bank.forex.application.result.ExchangeResult;
import com.macau.bank.forex.domain.entity.ForexTradeOrder;

import java.math.BigDecimal;

public interface ForexAppService {
    /**
     * 外币兑换交易
     */
    ExchangeResult exchange(ExchangeCmd cmd);

    void doGlobalTransaction(ExchangeCmd cmd, ForexTradeOrder order, BigDecimal sellCurrencyMopRate, BigDecimal buyCurrencyMopRate);
}
