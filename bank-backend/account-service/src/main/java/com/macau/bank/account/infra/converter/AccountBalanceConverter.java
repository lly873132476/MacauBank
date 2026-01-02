package com.macau.bank.account.infra.converter;

import com.macau.bank.account.domain.entity.AccountBalance;
import com.macau.bank.account.infra.persistent.po.AccountBalancePO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountBalanceConverter {
    AccountBalancePO toPO(AccountBalance entity);
    
    AccountBalance toEntity(AccountBalancePO po);
}
