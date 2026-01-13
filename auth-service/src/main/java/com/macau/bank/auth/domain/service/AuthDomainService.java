package com.macau.bank.auth.domain.service;

import com.macau.bank.auth.common.enums.AuthErrorCode;
import com.macau.bank.auth.domain.CustomerNoGenerator;
import com.macau.bank.auth.domain.entity.UserAuth;
import com.macau.bank.auth.domain.repository.UserAuthRepository;
import com.macau.bank.common.core.constant.CommonConstant;
import com.macau.bank.common.core.enums.UserAuthStatus;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.util.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 认证领域服务
 * <p>
 * 归属：Domain 层
 * 职责：用户认证核心逻辑（注册、登录、密码管理）
 * <p>
 * 重构说明：使用 UserAuthRepository 替代 UserAuthMapper，符合 DDD 分层规范
 */
@Service
public class AuthDomainService {

    @Resource
    private UserAuthRepository userAuthRepository;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Resource
    private CustomerNoGenerator customerNoGenerator;

    @Value("${auth.token.expire-seconds:7200}")
    private int tokenExpireSeconds;

    public String register(UserAuth userAuth) {
        // 1. 检查用户名是否已存在 (如果提供了用户名)
        if (userAuth.getUserName() != null && !userAuth.getUserName().isEmpty()) {
            Long count = userAuthRepository.countByUserName(userAuth.getUserName());
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }
        }

        // 1.1 检查手机号是否已存在
        Long mobileCount = userAuthRepository.countByMobile(userAuth.getMobile());
        if (mobileCount > 0) {
            throw new BusinessException("手机号已注册");
        }

        // 2. 加密密码
        String encryptedPassword = bCryptPasswordEncoder.encode(userAuth.getLoginPassword());

        // 3. 创建用户
        userAuth.setUserNo(customerNoGenerator.nextCustomerNo()); // 生成用户编号
        userAuth.setMobilePrefix(userAuth.getMobilePrefix()); // 默认区号，后续可扩展
        userAuth.setLoginPassword(encryptedPassword);
        userAuth.setStatus(UserAuthStatus.NORMAL);
        userAuth.setCreateTime(LocalDateTime.now());
        userAuth.setUpdateTime(LocalDateTime.now());

        try {
            userAuthRepository.save(userAuth);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户名或手机号已被注册");
        }

        return userAuth.getUserNo();
    }

    public String login(String userName, String password, String clientIp) {
        // 1. 查询用户（支持用户名或手机号登录）
        UserAuth user = userAuthRepository.findByUserNameOrMobile(userName, userName);
        if (user == null) {
            throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
        }

        // 2. 验证密码
        boolean valid = bCryptPasswordEncoder.matches(password, user.getLoginPassword());
        if (!valid) {
            throw new BusinessException(AuthErrorCode.PASSWORD_ERROR);
        }

        return generateTokenAndUpdateStatus(user, clientIp);
    }

    /**
     * 手机号直接登录 (已通过验证码校验)
     */
    public String loginByMobile(String mobile, String clientIp) {
        UserAuth user = userAuthRepository.findByMobile(mobile);
        if (user == null) {
            throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
        }

        return generateTokenAndUpdateStatus(user, clientIp);
    }

    private String generateTokenAndUpdateStatus(UserAuth user, String clientIp) {
        // 3. 检查用户状态
        if (user.getStatus() != UserAuthStatus.NORMAL) {
            if (user.getStatus() == UserAuthStatus.DISABLED) {
                throw new BusinessException(AuthErrorCode.USER_DISABLED);
            } else if (user.getStatus() == UserAuthStatus.FREEZE) {
                throw new BusinessException(AuthErrorCode.USER_FREEZE);
            } else {
                throw new BusinessException(AuthErrorCode.USER_STATE_ERROR);
            }
        }

        // 4. 更新最后登录时间和IP
        UserAuth loginInfo = new UserAuth();
        loginInfo.setId(user.getId());
        loginInfo.setLastLoginTime(LocalDateTime.now());
        loginInfo.setLastLoginIp(clientIp);
        userAuthRepository.update(loginInfo);

        return createToken(user.getUserNo());
    }

    /**
     * 创建token 并保存到Redis中
     */
    public String createToken(String userNo) {
        String token = UUID.randomUUID().toString().replace("-", ""); // 使用UUID生成Token
        String redisKey = CommonConstant.REDIS_TOKEN_PREFIX + token;
        redisUtil.setex(redisKey, tokenExpireSeconds, userNo);
        return token;
    }

    public boolean logout(String token) {
        String redisKey = CommonConstant.REDIS_TOKEN_PREFIX + token;
        redisUtil.del(redisKey);
        return true;
    }

    public boolean updateLoginPassword(String userNo, String oldPassword, String newPassword) {
        // 1. 查询用户（包含密码信息）
        UserAuth user = userAuthRepository.findByUserNo(userNo);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 验证旧密码
        boolean valid = bCryptPasswordEncoder.matches(oldPassword, user.getLoginPassword());
        if (!valid) {
            throw new BusinessException("原密码错误");
        }

        // 3. 加密新密码
        String encryptedPassword = bCryptPasswordEncoder.encode(newPassword);

        // 4. 更新密码
        UserAuth updateUser = new UserAuth();
        updateUser.setLoginPassword(encryptedPassword);
        updateUser.setUpdateTime(LocalDateTime.now());
        userAuthRepository.updateByUserNo(userNo, updateUser);
        return true;
    }

    public boolean updateTransPassword(String userNo, String transactionPassword) {
        // 1. 查询用户（包含盐值）
        UserAuth user = userAuthRepository.findByUserNo(userNo);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 加密交易密码
        String encryptedPassword = bCryptPasswordEncoder.encode(transactionPassword);

        // 3. 更新交易密码
        UserAuth updateUser = new UserAuth();
        updateUser.setTransactionPassword(encryptedPassword);
        updateUser.setUpdateTime(LocalDateTime.now());
        userAuthRepository.updateByUserNo(userNo, updateUser);
        return true;
    }

    public boolean verifyTransPassword(String userNo, String transactionPassword) {
        // 1. 查询用户（包含交易密码）
        UserAuth user = userAuthRepository.findByUserNo(userNo);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 验证交易密码
        if (user.getTransactionPassword() == null) {
            throw new BusinessException("请先设置交易密码");
        }

        return bCryptPasswordEncoder.matches(transactionPassword, user.getTransactionPassword());
    }

}