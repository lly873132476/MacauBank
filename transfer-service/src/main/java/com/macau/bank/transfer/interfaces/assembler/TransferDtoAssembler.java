package com.macau.bank.transfer.interfaces.assembler;

import com.macau.bank.api.dto.TransferRequest;
import com.macau.bank.transfer.application.command.TransferCmd;
import com.macau.bank.transfer.application.result.TransferOrderResult;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransferDtoAssembler {

    TransferOrderResult toResult(TransferOrder transferOrder);

    List<TransferOrderResult> toResultList(List<TransferOrder> transferOrderList);

    TransferRequest toRequest(TransferCmd cmd);
    TransferContext toContext(TransferRequest request);
}