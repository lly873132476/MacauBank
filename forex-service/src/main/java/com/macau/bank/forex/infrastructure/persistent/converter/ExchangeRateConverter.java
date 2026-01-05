package com.macau.bank.forex.infrastructure.persistent.converter;

import com.macau.bank.forex.domain.entity.ExchangeRate;
import com.macau.bank.forex.infrastructure.persistent.po.ExchangeRatePO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExchangeRateConverter {
    ExchangeRatePO toPO(ExchangeRate entity);
    ExchangeRate toEntity(ExchangeRatePO po);
    List<ExchangeRate> toEntityList(List<ExchangeRatePO> poList);
}
