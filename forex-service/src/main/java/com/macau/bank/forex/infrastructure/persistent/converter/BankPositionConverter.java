package com.macau.bank.forex.infrastructure.persistent.converter;

import com.macau.bank.forex.domain.entity.BankPosition;
import com.macau.bank.forex.infrastructure.persistent.po.BankPositionPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankPositionConverter {
    BankPositionPO toPO(BankPosition entity);
    BankPosition toEntity(BankPositionPO po);
}
