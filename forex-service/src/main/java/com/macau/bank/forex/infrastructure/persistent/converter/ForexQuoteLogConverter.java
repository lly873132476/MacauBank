package com.macau.bank.forex.infrastructure.persistent.converter;

import com.macau.bank.forex.domain.entity.ForexQuoteLog;
import com.macau.bank.forex.infrastructure.persistent.po.ForexQuoteLogPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ForexQuoteLogConverter {
    ForexQuoteLogPO toPO(ForexQuoteLog entity);
    ForexQuoteLog toEntity(ForexQuoteLogPO po);
}
