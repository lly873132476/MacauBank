package com.macau.bank.account.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.account.domain.entity.AccountCurrencyConfig;
import com.macau.bank.account.domain.repository.AccountCurrencyConfigRepository;
import com.macau.bank.account.infra.converter.AccountCurrencyConfigConverter;
import com.macau.bank.account.infra.mapper.AccountCurrencyConfigMapper;
import com.macau.bank.account.infra.persistent.po.AccountCurrencyConfigPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class AccountCurrencyConfigRepositoryImpl implements AccountCurrencyConfigRepository {

    @Resource
    private AccountCurrencyConfigMapper accountCurrencyConfigMapper;

    @Resource
    private AccountCurrencyConfigConverter accountCurrencyConfigConverter;

    @Override
    public AccountCurrencyConfig findByCurrencyCode(String currencyCode) {
        LambdaQueryWrapper<AccountCurrencyConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountCurrencyConfigPO::getCurrencyCode, currencyCode);
        return accountCurrencyConfigConverter.toEntity(accountCurrencyConfigMapper.selectOne(wrapper));
    }
}
