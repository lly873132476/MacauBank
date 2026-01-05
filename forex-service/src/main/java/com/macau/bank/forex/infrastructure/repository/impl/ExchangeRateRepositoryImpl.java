package com.macau.bank.forex.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.forex.common.enums.RateStatusEnum;
import com.macau.bank.forex.domain.entity.ExchangeRate;
import com.macau.bank.forex.domain.repository.ExchangeRateRepository;
import com.macau.bank.forex.infrastructure.mapper.ExchangeRateMapper;
import com.macau.bank.forex.infrastructure.persistent.converter.ExchangeRateConverter;
import com.macau.bank.forex.infrastructure.persistent.po.ExchangeRatePO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ExchangeRateRepositoryImpl implements ExchangeRateRepository {

    @Resource
    private ExchangeRateMapper exchangeRateMapper;

    @Resource
    private ExchangeRateConverter exchangeRateConverter;

    @Override
    public List<ExchangeRate> findAllActive() {
        List<ExchangeRatePO> pos = exchangeRateMapper.selectList(
                new LambdaQueryWrapper<ExchangeRatePO>()
                        .eq(ExchangeRatePO::getStatus, RateStatusEnum.EFFECTIVE)
                        .le(ExchangeRatePO::getEffectTime, LocalDateTime.now())
                        .and(w -> w.isNull(ExchangeRatePO::getExpireTime).or().gt(ExchangeRatePO::getExpireTime, LocalDateTime.now()))
        );
        return exchangeRateConverter.toEntityList(pos);
    }

    @Override
    public ExchangeRate findByPairCode(String pairCode) {
        ExchangeRatePO po = exchangeRateMapper.selectOne(
                new LambdaQueryWrapper<ExchangeRatePO>()
                        .eq(ExchangeRatePO::getCurrencyPair, pairCode)
                        .eq(ExchangeRatePO::getStatus, RateStatusEnum.EFFECTIVE)
                        .le(ExchangeRatePO::getEffectTime, LocalDateTime.now())
                        .and(w -> w.isNull(ExchangeRatePO::getExpireTime).or().gt(ExchangeRatePO::getExpireTime, LocalDateTime.now()))
                        .orderByDesc(ExchangeRatePO::getEffectTime)
                        .last("LIMIT 1")
        );
        return exchangeRateConverter.toEntity(po);
    }
}
