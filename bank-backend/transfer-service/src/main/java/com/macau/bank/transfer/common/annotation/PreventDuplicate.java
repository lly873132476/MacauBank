package com.macau.bank.transfer.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交注解
 * 
 * <p>
 * 使用 Redis Token 实现幂等性，防止用户重复提交
 * </p>
 * 
 * @author MacauBank
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventDuplicate {

    /**
     * Redis Key 前缀
     */
    String prefix() default "prevent:duplicate:";

    /**
     * 过期时间
     */
    long timeout() default 5;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 提示信息
     */
    String message() default "请勿重复提交";
}
