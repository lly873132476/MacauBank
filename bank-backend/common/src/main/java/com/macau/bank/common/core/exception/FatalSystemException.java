package com.macau.bank.common.core.exception;

/**
 * 致命系统异常
 * 触发场景：数据不一致、幂等参数被篡改、核心配置缺失、数据库约束被击穿
 * 处理策略：
 * 1. 打印 ERROR 级别日志（带堆栈）
 * 2. 触发 钉钉/邮件/短信 报警
 * 3. 事务必须回滚
 * 4. 禁止自动重试
 */
public class FatalSystemException extends RuntimeException {

    private String code;

    public FatalSystemException(String message) {
        super(message);
        this.code = "FATAL_ERROR";
    }

    public FatalSystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = "FATAL_ERROR";
    }
    
    //以此类推，可以加更多构造函数
}