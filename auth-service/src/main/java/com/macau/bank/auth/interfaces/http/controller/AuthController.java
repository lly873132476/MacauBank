package com.macau.bank.auth.interfaces.http.controller;

import com.macau.bank.auth.interfaces.http.assembler.AuthWebAssembler;
import com.macau.bank.auth.application.command.LoginCmd;
import com.macau.bank.auth.application.command.RegisterCmd;
import com.macau.bank.auth.application.result.LoginResult;
import com.macau.bank.auth.application.result.RegisterResult;
import com.macau.bank.auth.application.service.AuthAppService;
import com.macau.bank.auth.interfaces.http.request.LoginRequest;
import com.macau.bank.auth.interfaces.http.request.RegisterRequest;
import com.macau.bank.auth.interfaces.http.request.UpdatePasswordRequest;
import com.macau.bank.auth.interfaces.http.request.UpdateTransactionPasswordRequest;
import com.macau.bank.auth.interfaces.http.response.LoginResponse;
import com.macau.bank.auth.interfaces.http.response.RegisterResponse;
import com.macau.bank.common.core.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * DDD 分层职责：
 * - 只负责 HTTP 请求的接收和响应
 * - 不包含任何业务逻辑
 * - 所有业务逻辑委托给 Application Service
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Resource
    private AuthAppService authAppService;

    @Resource
    private AuthWebAssembler authWebAssembler;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<RegisterResponse> register(@RequestBody @Validated RegisterRequest request) {
        // 1. 【拆包】 Request -> Cmd
        RegisterCmd cmd = authWebAssembler.toCmd(request);

        // 2. 【办事】 调用应用层服务
        RegisterResult result = authAppService.register(cmd);

        // 3. 【包装】 Result -> Response
        RegisterResponse response = authWebAssembler.toResponse(result);

        return Result.success(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Validated LoginRequest request) {
        log.info("应用服务 - 用户登录: userName={}", request.getUserName());

        // 1. 【拆包】 Request -> Cmd
        LoginCmd loginCmd = authWebAssembler.toCmd(request);

        // 2. 【办事】 调用应用层服务
        LoginResult loginResult = authAppService.login(loginCmd);

        // 3. 【包装】 Result -> Response
        LoginResponse response = authWebAssembler.toResponse(loginResult);

        return Result.success("登录成功", response);
    }

    /**
     * 用户登出
     *
     * @param token 当前用户token（由拦截器设置）
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") @NotBlank(message = "Token不能为空") String token) {
        authAppService.logout(token);
        return Result.success("登出成功", null);
    }

    /**
     * 验证Token是否有效
     */
    @GetMapping("/token/verify")
    public Result<Boolean> verifyToken() {
        return Result.success(true);
    }

    /**
     * 修改登录密码
     *
     * @param requestDTO 修改密码请求
     */
    @PostMapping("/password/update")
    public Result<Void> updatePassword(@RequestBody @Validated UpdatePasswordRequest requestDTO) {
        authAppService.updateLoginPassword(requestDTO.getUserNo(), requestDTO.getOldPassword(), requestDTO.getNewPassword());
        return Result.success("密码修改成功", null);
    }

    /**
     * 设置/修改交易密码
     *
     * @param requestDTO 交易密码请求
     */
    @PostMapping("/transPwd/update")
    public Result<Void> updateTransPassword(@RequestBody @Validated UpdateTransactionPasswordRequest requestDTO) {
        authAppService.updateTransPassword(requestDTO.getUserNo(), requestDTO.getPassword());
        return Result.success("交易密码设置成功", null);
    }
}