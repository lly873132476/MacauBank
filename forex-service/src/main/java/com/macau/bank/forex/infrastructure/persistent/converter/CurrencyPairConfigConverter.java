package com.macau.bank.forex.infrastructure.persistent.converter;

import com.macau.bank.forex.domain.entity.CurrencyPairConfig;
import com.macau.bank.forex.infrastructure.persistent.po.CurrencyPairConfigPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyPairConfigConverter {
    CurrencyPairConfigPO toPO(CurrencyPairConfig entity);
    CurrencyPairConfig toEntity(CurrencyPairConfigPO po);
}
