package com.macau.bank.common.core.result;

/**
 * 错误码接口
 */
public interface IResultCode {

    /**
     * 错误码
     */
    Integer getCode();

    /**
     * 错误信息
     */
    String getMessage();

    /**
     * 获取国际化Key
     */
    String getI18nKey();
}