package com.macau.bank.forex.infrastructure.persistent.converter;

import com.macau.bank.forex.domain.entity.ForexTradeOrder;
import com.macau.bank.forex.infrastructure.persistent.po.ForexTradeOrderPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ForexTradeOrderConverter {
    ForexTradeOrderPO toPO(ForexTradeOrder entity);
    ForexTradeOrder toEntity(ForexTradeOrderPO po);
}
