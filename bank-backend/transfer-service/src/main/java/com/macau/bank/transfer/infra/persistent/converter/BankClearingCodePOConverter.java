package com.macau.bank.transfer.infra.persistent.converter;

import com.macau.bank.transfer.domain.entity.BankClearingCode;
import com.macau.bank.transfer.infra.persistent.po.BankClearingCodePO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankClearingCodePOConverter {

    BankClearingCodePO toPO(BankClearingCode entity);

    BankClearingCode toEntity(BankClearingCodePO po);
}
