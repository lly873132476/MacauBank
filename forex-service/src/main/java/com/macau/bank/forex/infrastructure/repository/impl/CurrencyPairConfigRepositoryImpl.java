package com.macau.bank.forex.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.forex.domain.entity.CurrencyPairConfig;
import com.macau.bank.forex.domain.repository.CurrencyPairConfigRepository;
import com.macau.bank.forex.infrastructure.mapper.CurrencyPairConfigMapper;
import com.macau.bank.forex.infrastructure.persistent.converter.CurrencyPairConfigConverter;
import com.macau.bank.forex.infrastructure.persistent.po.CurrencyPairConfigPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class CurrencyPairConfigRepositoryImpl implements CurrencyPairConfigRepository {

    @Resource
    private CurrencyPairConfigMapper currencyPairConfigMapper;

    @Resource
    private CurrencyPairConfigConverter currencyPairConfigConverter;

    @Override
    public CurrencyPairConfig findByPairCode(String pairCode) {
        CurrencyPairConfigPO po = currencyPairConfigMapper.selectOne(
                new LambdaQueryWrapper<CurrencyPairConfigPO>().eq(CurrencyPairConfigPO::getPairCode, pairCode)
        );
        return currencyPairConfigConverter.toEntity(po);
    }
}
