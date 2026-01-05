package com.macau.bank.message.common.result;

import com.macau.bank.common.core.result.IResultCode;
import lombok.Getter;

/**
 * Message服务错误码
 */
@Getter
public enum MessageErrorCode implements IResultCode {

    // ==================== Message服务 - 业务错误 (6030xx) ====================
    MESSAGE_SEND_FAILED(603001, "消息发送失败", "message.send.failed"),
    TEMPLATE_NOT_FOUND(603002, "消息模板不存在", "message.template.not.found");

    private final Integer code;
    private final String message;
    private final String i18nKey;

    MessageErrorCode(Integer code, String message, String i18nKey) {
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }
}