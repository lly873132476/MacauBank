package com.macau.bank.transfer.infra.persistent.converter;

import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.infra.persistent.po.TransferOrderPO;
import org.mapstruct.Mapper;

/**
 * 转账订单对象转换器
 * <p>
 * 负责 Entity <-> PO 的互相转换
 */
@Mapper(componentModel = "spring")
public interface TransferOrderPOConverter {

    TransferOrderPO toPO(TransferOrder entity);

    TransferOrder toEntity(TransferOrderPO po);
}
