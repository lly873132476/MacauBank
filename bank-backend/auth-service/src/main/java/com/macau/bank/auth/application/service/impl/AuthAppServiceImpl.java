package com.macau.bank.auth.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.api.account.service.AccountRpcService;
import com.macau.bank.api.user.request.UserCreateRpcRequest;
import com.macau.bank.api.user.service.UserRpcService;
import com.macau.bank.auth.application.assembler.AuthDomainAssembler;
import com.macau.bank.auth.application.command.LoginCmd;
import com.macau.bank.auth.application.command.RegisterCmd;
import com.macau.bank.auth.application.result.LoginResult;
import com.macau.bank.auth.application.result.RegisterResult;
import com.macau.bank.auth.application.service.AuthAppService;
import com.macau.bank.auth.domain.entity.UserAuth;
import com.macau.bank.auth.domain.service.AuthDomainService;
import com.macau.bank.auth.infrastructure.mapper.UserAuthMapper;
import com.macau.bank.common.core.enums.LoginType;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.result.Result;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 认证应用服务实现
 * 负责：
 * 1. 参数校验
 * 2. 业务流程编排
 * 3. 调用领域服务
 * 4. 跨服务协调（创建账户）
 */
@Slf4j
@Service
public class AuthAppServiceImpl implements AuthAppService {

    @Resource
    private AuthDomainService authDomainService;

    @DubboReference
    private AccountRpcService accountRpcService;

    @DubboReference
    private UserRpcService userRpcService;
    
    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private AuthDomainAssembler authDomainAssembler;

    @Override
    @GlobalTransactional(name = "auth-register-create-account", rollbackFor = Exception.class)
    public RegisterResult register(RegisterCmd registerCmd) {
        log.info("应用服务 - 用户注册 (开启Seata全局事务): mobile={}", registerCmd.getMobile());

        // 没传手机区号，默认为澳门地区
        if (!StringUtils.hasText(registerCmd.getMobilePrefix())) {
            registerCmd.setMobilePrefix("+853");
        }

        // 校验验证码 (Mock)
        if (!"123456".equals(registerCmd.getVerifyCode())) {
            throw new BusinessException("验证码错误");
        }

        // 自动生成用户名 (如果为空)
        if (!StringUtils.hasText(registerCmd.getUserName())) {
            registerCmd.setUserName("MacosBank@" + registerCmd.getMobile());
        }

        // 1. 转成 Entity
        UserAuth userAuth = authDomainAssembler.toEntity(registerCmd);

        // 2. 本地事务：插入 UserAuth 表
        String userNo = authDomainService.register(userAuth);

        // 3. 远程 RPC：调用 User 服务创建用户档案
        log.info("应用服务 - 创建用户档案: userNo={}", userNo);
        UserCreateRpcRequest request = authDomainAssembler.toRpcRequest(registerCmd);
        request.setUserNo(userNo);

        Result<Void> userRpcResult = userRpcService.createUserInfo(request);
        // 判断 RPC 业务是否成功
        if (!userRpcResult.isSuccess()) {
            // 关键点：这里必须抛出异常！
            // 只有抛出异常，Seata 才会捕获到并通知 TC (事务协调器) 进行全局回滚
            // 同时，我们把 User 服务返回的具体错误信息透传出去
            throw new BusinessException(userRpcResult.getCode(), userRpcResult.getMessage());
        }

        // 4. 远程 RPC：调用 User 服务创建用户档案
        String token = authDomainService.createToken(userNo);

        // 5. 构建响应
        return RegisterResult.builder()
                .userNo(userNo)
                .userName(request.getUserName())
                .token(token)
                .build();
    }

    @Override
    public LoginResult login(LoginCmd loginCmd) {
        log.info("应用服务 - 用户登录: userName={}, type={}", loginCmd.getUserName(), loginCmd.getLoginType());

        String token;
        UserAuth userAuth = null;

        if (LoginType.SMS == loginCmd.getLoginType()) {
            // 验证码登录
            if (!"123456".equals(loginCmd.getVerifyCode())) {
                throw new BusinessException("验证码错误");
            }
            // 手机号直接登录
            token = authDomainService.loginByMobile(loginCmd.getUserName(), loginCmd.getClientIp());
            
            // 查询用户信息用于返回
            userAuth = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                    .eq(UserAuth::getMobile, loginCmd.getUserName()));
        } else {
            // 密码登录
            token = authDomainService.login(loginCmd.getUserName(), loginCmd.getPassword(), loginCmd.getClientIp());
            
            // 查询用户信息
            userAuth = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                    .eq(UserAuth::getUserName, loginCmd.getUserName())
                    .or()
                    .eq(UserAuth::getMobile, loginCmd.getUserName())
            );
        }

        log.info("应用服务 - 登录成功: userNo={}, token={}", userAuth.getUserNo(), token);

        // 3. 构建响应
        return LoginResult.builder()
                .userNo(userAuth.getUserNo())
                .userName(userAuth.getUserName())
                .token(token)
                .build();
    }

    @Override
    public void logout(String token) {
        log.info("应用服务 - 用户登出");
        
        // 2. 调用认证服务登出
        authDomainService.logout(token);
        log.info("应用服务 - 登出成功: token={}", token);
    }

    @Override
    public void updateLoginPassword(String userNo, String oldPassword, String newPassword) {
        log.info("应用服务 - 修改登录密码: userNo={}", userNo);
        
        // 调用认证服务修改密码
        boolean success = authDomainService.updateLoginPassword(userNo, oldPassword, newPassword);
        
        if (!success) {
            throw new BusinessException("密码修改失败");
        }
        
        log.info("应用服务 - 密码修改成功: userNo={}", userNo);
    }

    @Override
    public void updateTransPassword(String userNo, String transactionPassword) {
        log.info("应用服务 - 设置交易密码: userNo={}", userNo);
        
        // 参数校验
        if (!StringUtils.hasText(userNo)) {
            throw new BusinessException("用户不存在");
        }

        // 调用认证服务设置交易密码
        boolean success = authDomainService.updateTransPassword(userNo, transactionPassword);
        
        if (!success) {
            throw new BusinessException("交易密码设置失败");
        }
        
        log.info("应用服务 - 交易密码设置成功: userNo={}", userNo);
    }

}