package com.macau.bank.common.core.exception;

import lombok.Getter;

/**
 * 系统异常 (System Exception)
 * 场景：网络超时、数据库挂了、NPE、RPC调用失败、上游服务返回500
 * 处理：
 * 1. 必须打印 ERROR 日志 (带堆栈)
 * 2. 必须触发分布式事务回滚
 * 3. 视情况触发熔断/报警
 * 4. 前端展示：“系统繁忙，请稍后重试” (不展示具体堆栈)
 */
@Getter
public class SystemException extends RuntimeException {

    private final String code;

    // 默认构造：使用通用的 500 错误码
    public SystemException(String message) {
        super(message);
        this.code = "500"; // 或者用 ResultCode.SYSTEM_ERROR.getCode()
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = "500";
    }

    // 允许自定义错误码（比如 503 Service Unavailable）
    public SystemException(String code, String message) {
        super(message);
        this.code = code;
    }
}