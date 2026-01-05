package com.macau.bank.common.core.result;

import lombok.Getter;

/**
 * 统一错误码 (通用)
 * /p
 * 各个服务的错误码请在各自服务的 common.result 包下定义
 * /p
 * 错误码规则：6位数字（无前导0，避免八进制问题）
 * 格式：应用码(2位) + 错误类型(2位) + 错误序号(2位)
 * /p
 * 应用码分配：10-99
 * 10 - auth-service      (认证服务)
 * 20 - account-service   (账户服务)
 * 30 - transfer-service  (转账服务)
 * 40 - forex-service     (外汇服务)
 * 50 - user-service      (用户服务)
 * 60 - message-service   (消息服务)
 * 99 - common            (通用/网关)
 */
@Getter
public enum ResultCode implements IResultCode {
    
    // ==================== 通用错误码 (99xxxx) ====================
    SUCCESS(200, "成功", "common.success"),
    FAIL(991001, "系统错误", "common.error.system"),
    PARAM_ERROR(991002, "参数错误", "common.error.param"),
    
    // ==================== Auth通用错误 (1020xx) ====================
    UNAUTHORIZED(102001, "未登录，请先登录", "common.error.unauthorized"),
    TOKEN_INVALID(102002, "Token无效或已过期，请重新登录", "common.error.token.invalid"),
    
    // ==================== 数据错误 (9940xx) ====================
    DATA_NOT_FOUND(994001, "数据不存在", "common.error.data.not.found"),
    DATA_ALREADY_EXISTS(994002, "数据已存在", "common.error.data.already.exists"),
    DATA_INVALID(994003, "数据无效", "common.error.data.invalid");
    
    private final Integer code;
    private final String message;
    private final String i18nKey;
    
    ResultCode(Integer code, String message, String i18nKey) {
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }

    /**
     * 判断是否为认证相关错误
     * 所有应用的认证错误都是 xx20xx 格式（错误类型为20）
     */
    public boolean isAuthError() {
        // 获取错误类型（中间2位）
        int errorType = (code / 100) % 100;
        return errorType == 20;
    }
    
    /**
     * 获取应用码（前2位）
     */
    public int getAppCode() {
        return code / 10000;
    }
    
    /**
     * 获取错误类型（中间2位）
     */
    public int getErrorType() {
        return (code / 100) % 100;
    }
    
    /**
     * 获取错误序号（后2位）
     */
    public int getErrorNumber() {
        return code % 100;
    }
    
}
