package com.macau.bank.transfer.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 * <p>
 * 标记需要记录审计日志的方法。金融系统中，所有关键操作都需要留痕，
 * 用于事后追溯和合规审计。
 * <p>
 * 使用示例：
 * 
 * <pre>
 * {@code @Auditable(action = "TRANSFER_SUBMIT", targetType = "TRANSFER_ORDER")}
 * public TransferResult submitTransfer(TransferCmd cmd) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * 操作类型
     * <p>
     * 示例：TRANSFER_SUBMIT, TRANSFER_QUERY, ACCOUNT_FREEZE
     */
    String action();

    /**
     * 目标类型
     * <p>
     * 示例：TRANSFER_ORDER, ACCOUNT, PAYEE
     */
    String targetType() default "";

    /**
     * 目标ID的 SpEL 表达式
     * <p>
     * 用于从方法参数中提取目标ID。
     * 示例：#cmd.idempotentKey, #id
     */
    String targetIdExpr() default "";

    /**
     * 是否记录请求参数
     */
    boolean logRequest() default true;

    /**
     * 是否记录响应结果
     */
    boolean logResponse() default true;
}
