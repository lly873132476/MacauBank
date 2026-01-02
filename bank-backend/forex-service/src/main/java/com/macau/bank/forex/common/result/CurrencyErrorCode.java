package com.macau.bank.forex.common.result;

import com.macau.bank.common.core.result.IResultCode;
import lombok.Getter;

/**
 * Currency服务错误码
 */
@Getter
public enum CurrencyErrorCode implements IResultCode {

    // ==================== Currency服务 - 业务错误 (4030xx) ====================
    CURRENCY_NOT_SUPPORTED(403001, "不支持该货币", "currency.not.supported"),
    EXCHANGE_RATE_NOT_FOUND(403002, "汇率不存在", "currency.exchange.rate.not.found");

    private final Integer code;
    private final String message;
    private final String i18nKey;

    CurrencyErrorCode(Integer code, String message, String i18nKey) {
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }
}