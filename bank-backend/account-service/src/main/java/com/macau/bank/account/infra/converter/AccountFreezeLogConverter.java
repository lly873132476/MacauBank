package com.macau.bank.account.infra.converter;

import com.macau.bank.account.domain.entity.AccountFreezeLog;
import com.macau.bank.account.infra.persistent.po.AccountFreezeLogPO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountFreezeLogConverter {
    AccountFreezeLogPO toPO(AccountFreezeLog entity);

    AccountFreezeLog toEntity(AccountFreezeLogPO po);

    List<AccountFreezeLog> toEntityList(List<AccountFreezeLogPO> poList);
}
