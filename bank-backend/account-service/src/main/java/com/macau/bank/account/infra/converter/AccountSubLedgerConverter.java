package com.macau.bank.account.infra.converter;

import com.macau.bank.account.domain.entity.AccountSubLedger;
import com.macau.bank.account.infra.persistent.po.AccountSubLedgerPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountSubLedgerConverter {
    AccountSubLedgerPO toPO(AccountSubLedger entity);

    AccountSubLedger toEntity(AccountSubLedgerPO po);
}
