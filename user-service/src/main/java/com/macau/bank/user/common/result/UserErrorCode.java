package com.macau.bank.user.common.result;

import com.macau.bank.common.core.result.IResultCode;
import lombok.Getter;

/**
 * User服务错误码
 */
@Getter
public enum UserErrorCode implements IResultCode {

    // ==================== User服务 - 业务错误 (5030xx) ====================
    USER_INFO_NOT_FOUND(503001, "用户信息不存在", "user.info.not.found");

    private final Integer code;
    private final String message;
    private final String i18nKey;

    UserErrorCode(Integer code, String message, String i18nKey) {
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }
}