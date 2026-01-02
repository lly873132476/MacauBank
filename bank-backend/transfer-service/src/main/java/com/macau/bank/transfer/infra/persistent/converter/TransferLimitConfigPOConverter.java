package com.macau.bank.transfer.infra.persistent.converter;

import com.macau.bank.transfer.domain.entity.TransferLimitConfig;
import com.macau.bank.transfer.infra.persistent.po.TransferLimitConfigPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferLimitConfigPOConverter {

    TransferLimitConfigPO toPO(TransferLimitConfig entity);

    TransferLimitConfig toEntity(TransferLimitConfigPO po);
}
