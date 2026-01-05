package com.macau.bank.transfer.infra.persistent.converter;

import com.macau.bank.transfer.domain.entity.TransferPayeeBook;
import com.macau.bank.transfer.infra.persistent.po.TransferPayeeBookPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransferPayeeBookPOConverter {

    TransferPayeeBookPO toPO(TransferPayeeBook entity);

    TransferPayeeBook toEntity(TransferPayeeBookPO po);
}
