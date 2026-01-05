package com.macau.bank.auth.application.assembler;

import com.macau.bank.api.user.request.UserCreateRpcRequest;
import com.macau.bank.auth.application.command.RegisterCmd;
import com.macau.bank.auth.domain.entity.UserAuth;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthDomainAssembler {

    @Mapping(target = "userNo", ignore = true)
    UserCreateRpcRequest toRpcRequest(RegisterCmd cmd);

    @Mapping(source = "password", target = "loginPassword")
    UserAuth toEntity(RegisterCmd registerCmd);
}