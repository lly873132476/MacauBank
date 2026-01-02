package com.macau.bank.forex.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.forex.domain.entity.BankPosition;
import com.macau.bank.forex.domain.repository.BankPositionRepository;
import com.macau.bank.forex.infrastructure.mapper.BankPositionMapper;
import com.macau.bank.forex.infrastructure.persistent.converter.BankPositionConverter;
import com.macau.bank.forex.infrastructure.persistent.po.BankPositionPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class BankPositionRepositoryImpl implements BankPositionRepository {

    @Resource
    private BankPositionMapper bankPositionMapper;

    @Resource
    private BankPositionConverter bankPositionConverter;

    @Override
    public BankPosition findByCurrency(String currencyCode) {
        BankPositionPO po = bankPositionMapper.selectOne(
                new LambdaQueryWrapper<BankPositionPO>().eq(BankPositionPO::getCurrencyCode, currencyCode)
        );
        return bankPositionConverter.toEntity(po);
    }

    @Override
    public boolean update(BankPosition bankPosition) {
        BankPositionPO po = bankPositionConverter.toPO(bankPosition);
        // updateById handles @Version check
        return bankPositionMapper.updateById(po) > 0;
    }
}
