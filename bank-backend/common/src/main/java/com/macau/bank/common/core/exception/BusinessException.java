package com.macau.bank.common.core.exception;

import com.macau.bank.common.core.result.IResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 国际化Key
     */
    private String i18nKey;

    /**
     * 国际化参数
     */
    private Object[] args;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BusinessException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.i18nKey = resultCode.getI18nKey();
    }
    
    // ==================== 国际化支持 ====================

    public BusinessException(String message, String i18nKey) {
        super(message);
        this.code = 500;
        this.message = message;
        this.i18nKey = i18nKey;
    }
    
    public BusinessException(String message, String i18nKey, Object[] args) {
        super(message);
        this.code = 500;
        this.message = message;
        this.i18nKey = i18nKey;
        this.args = args;
    }

    public BusinessException(Integer code, String message, String i18nKey) {
        super(message);
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }
    
    public BusinessException(Integer code, String message, String i18nKey, Object[] args) {
        super(message);
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
        this.args = args;
    }
    
    public BusinessException(IResultCode resultCode, String i18nKey) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.i18nKey = i18nKey;
    }
    
    public BusinessException(IResultCode resultCode, Object[] args) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.i18nKey = resultCode.getI18nKey();
        this.args = args;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
