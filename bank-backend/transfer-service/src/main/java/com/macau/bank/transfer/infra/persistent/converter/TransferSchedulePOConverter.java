package com.macau.bank.transfer.infra.persistent.converter;

import com.macau.bank.transfer.domain.entity.TransferSchedule;
import com.macau.bank.transfer.infra.persistent.po.TransferSchedulePO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferSchedulePOConverter {

    TransferSchedulePO toPO(TransferSchedule entity);

    TransferSchedule toEntity(TransferSchedulePO po);
}
