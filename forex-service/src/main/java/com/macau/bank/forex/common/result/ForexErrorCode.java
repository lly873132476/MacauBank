package com.macau.bank.forex.common.result;

import com.macau.bank.common.core.result.IResultCode;
import lombok.Getter;

/**
 * Forex 外汇服务错误码
 */
@Getter
public enum ForexErrorCode implements IResultCode {

    // ==================== Forex服务 - 业务错误 (4040xx) ====================
    USER_CONTEXT_MISSING(404001, "用户上下文缺失，请重新登录", "forex.user.context.missing"),
    ACCOUNT_NOT_BELONG_TO_USER(404002, "账户不属于当前用户", "forex.account.not.belong.to.user"),
    DUPLICATE_REQUEST(404003, "重复请求，请勿重复提交", "forex.duplicate.request"),
    ACCOUNT_NOT_FOUND(404004, "账户不存在", "forex.account.not.found");

    private final Integer code;
    private final String message;
    private final String i18nKey;

    ForexErrorCode(Integer code, String message, String i18nKey) {
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }
}

