package com.macau.bank.account.application.assembler;

import com.macau.bank.account.application.result.TransactionFlowResult;
import com.macau.bank.account.domain.entity.AccountSubLedger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionFlowDomainAssembler {

    @Mapping(target = "direction", source = "cdFlag")
    TransactionFlowResult toResult(AccountSubLedger entity);

    List<TransactionFlowResult> toResultList(List<AccountSubLedger> entityList);
}
