package com.macau.bank.auth.application.service;

import com.macau.bank.auth.application.command.LoginCmd;
import com.macau.bank.auth.application.command.RegisterCmd;
import com.macau.bank.auth.application.result.LoginResult;
import com.macau.bank.auth.application.result.RegisterResult;

/**
 * 认证应用服务接口
 */
public interface AuthAppService {

    /**
     * 用户注册
     */
    RegisterResult register(RegisterCmd request);

    /**
     * 用户登录
     */
    LoginResult login(LoginCmd loginCmd);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 更新登录密码
     */
    void updateLoginPassword(String userNo, String oldPassword, String newPassword);

    /**
     * 更新交易密码
     */
    void updateTransPassword(String userNo, String transactionPassword);
}
