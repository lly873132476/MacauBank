package com.macau.bank.account.infra.converter;

import com.macau.bank.account.domain.entity.AccountInfo;
import com.macau.bank.account.infra.persistent.po.AccountInfoPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountInfoConverter {
    AccountInfoPO toPO(AccountInfo entity);

    AccountInfo toEntity(AccountInfoPO po);
}
