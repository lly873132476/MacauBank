package com.macau.bank.forex.application.assembler;

import com.macau.bank.forex.application.result.ExchangeResult;
import com.macau.bank.forex.domain.entity.ForexTradeOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ForexDomainAssembler {

    @Mapping(target = "transTime", source = "createTime")
    ExchangeResult toResult(ForexTradeOrder order);
}
