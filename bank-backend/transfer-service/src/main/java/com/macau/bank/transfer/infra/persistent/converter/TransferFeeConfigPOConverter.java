package com.macau.bank.transfer.infra.persistent.converter;

import com.macau.bank.transfer.domain.entity.TransferFeeConfig;
import com.macau.bank.transfer.infra.persistent.po.TransferFeeConfigPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferFeeConfigPOConverter {

    TransferFeeConfigPO toPO(TransferFeeConfig entity);

    TransferFeeConfig toEntity(TransferFeeConfigPO po);
}
