package com.macau.bank.transfer.application.assembler;

import com.macau.bank.transfer.application.command.AddPayeeCmd;
import com.macau.bank.transfer.application.command.UpdatePayeeCmd;
import com.macau.bank.transfer.application.result.PayeeResult;
import com.macau.bank.transfer.domain.entity.TransferPayeeBook;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransferAssembler {

    @Mapping(target = "id", ignore = true)
    TransferPayeeBook toPayeeEntity(AddPayeeCmd cmd);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    TransferPayeeBook toPayeeEntity(UpdatePayeeCmd cmd);

    List<PayeeResult> toPayeeResultList(List<TransferPayeeBook> records);
}