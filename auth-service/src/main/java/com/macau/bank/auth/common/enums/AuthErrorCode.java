package com.macau.bank.auth.common.enums;

import com.macau.bank.common.core.result.IResultCode;
import lombok.Getter;

/**
 * auth-service认证中心专用错误码
 * <p>
 * 范围建议：20000 ~ 29999 (防止与其他模块冲突)
 * 1xxxx: 通用错误
 * 2xxxx: Auth 模块
 * 3xxxx: User 模块
 * 4xxxx: Account 模块
 * </p>
 */
@Getter
public enum AuthErrorCode implements IResultCode {

    // ==================== Auth服务 - 认证错误 (1020xx) ====================
    TOKEN_EXPIRED(102003, "Token已过期，请重新登录", "auth.token.expired"),
    PERMISSION_DENIED(102004, "权限不足", "auth.permission.denied"),
    USER_DISABLED(102005, "用户已被禁用", "auth.user.disabled"),
    USER_FREEZE(102006, "用户已被冻结", "auth.user.freeze"),
    USER_STATE_ERROR(102007, "用户状态异常", "auth.user.state.error"),

    // ==================== Auth服务 - 业务错误 (1030xx) ====================
    USER_NOT_FOUND(103001, "用户不存在", "auth.user.not.found"),
    USER_ALREADY_EXISTS(103002, "用户已存在", "auth.user.already.exists"),
    PASSWORD_ERROR(103003, "密码错误", "auth.password.error"),
    OLD_PASSWORD_ERROR(103004, "原密码错误", "auth.old.password.error"),
    TRANSACTION_PASSWORD_ERROR(103005, "交易密码错误", "auth.transaction.password.error"),
    TRANSACTION_PASSWORD_NOT_SET(103006, "未设置交易密码", "auth.transaction.password.not.set"),

    USERNAME_NOT_EMPTY(103007, "用户名不能为空", "auth.username.not.empty"),
    PASSWORD_NOT_EMPTY(103008, "密码不能为空", "auth.password.not.empty");

    private final Integer code;
    private final String message;
    private final String i18nKey;
    
    AuthErrorCode(Integer code, String message, String i18nKey) {
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }
}