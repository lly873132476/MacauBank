package com.macau.bank.gateway.common.result;

import com.macau.bank.common.core.result.IResultCode;
import lombok.Getter;

/**
 * Gateway服务错误码
 */
@Getter
public enum GatewayErrorCode implements IResultCode {

    // ==================== Gateway服务 - 网关错误 (9950xx) ====================
    GATEWAY_ERROR(995001, "网关异常", "gateway.error"),
    SERVICE_UNAVAILABLE(995002, "服务不可用", "gateway.service.unavailable"),
    GATEWAY_TIMEOUT(995003, "网关超时", "gateway.timeout");

    private final Integer code;
    private final String message;
    private final String i18nKey;

    GatewayErrorCode(Integer code, String message, String i18nKey) {
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }
}