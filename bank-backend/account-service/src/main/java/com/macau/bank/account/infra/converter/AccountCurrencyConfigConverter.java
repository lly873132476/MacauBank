package com.macau.bank.account.infra.converter;

import com.macau.bank.account.domain.entity.AccountCurrencyConfig;
import com.macau.bank.account.infra.persistent.po.AccountCurrencyConfigPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountCurrencyConfigConverter {
    AccountCurrencyConfigPO toPO(AccountCurrencyConfig entity);

    AccountCurrencyConfig toEntity(AccountCurrencyConfigPO po);
}
