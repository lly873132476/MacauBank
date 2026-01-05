package com.macau.bank.user.interfaces.rpc.assembler;

import com.macau.bank.api.user.request.UserCreateRpcRequest;
import com.macau.bank.api.user.response.UserInfoRpcResponse;
import com.macau.bank.user.application.command.CreateUserInfoCmd;
import com.macau.bank.user.domain.entity.UserInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRpcAssembler {

    CreateUserInfoCmd toCmd(UserCreateRpcRequest request);

    UserInfoRpcResponse toRpc(UserInfo userInfo);
}