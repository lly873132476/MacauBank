package com.macau.bank.auth.interfaces.http.assembler;

import com.macau.bank.auth.application.command.LoginCmd;
import com.macau.bank.auth.application.command.RegisterCmd;
import com.macau.bank.auth.application.result.LoginResult;
import com.macau.bank.auth.application.result.RegisterResult;
import com.macau.bank.auth.interfaces.http.request.LoginRequest;
import com.macau.bank.auth.interfaces.http.request.RegisterRequest;
import com.macau.bank.auth.interfaces.http.response.LoginResponse;
import com.macau.bank.auth.interfaces.http.response.RegisterResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthWebAssembler {
    
    // Request -> Cmd
    RegisterCmd toCmd(RegisterRequest request);

    LoginCmd toCmd(LoginRequest request);

    // Result -> Response
    RegisterResponse toResponse(RegisterResult result);

    LoginResponse toResponse(LoginResult result);
}