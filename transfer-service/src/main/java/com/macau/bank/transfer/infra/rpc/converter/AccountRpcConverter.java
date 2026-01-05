package com.macau.bank.transfer.infra.rpc.converter;

import com.macau.bank.api.account.response.AccountInfoRpcResponse;
import com.macau.bank.transfer.domain.model.AccountSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountRpcConverter {

    AccountSnapshot toSnapshot(AccountInfoRpcResponse response);
}