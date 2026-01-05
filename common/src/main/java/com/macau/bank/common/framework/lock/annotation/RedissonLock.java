package com.macau.bank.common.framework.lock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonLock {
    
    /** 锁的 Key，支持 SpEL 表达式，例如 "#cmd.userNo" */
    String key();

    /** 等待时间，默认 0 秒，拿不到锁直接报错/跳过 */
    long waitTime() default -1;

    /** 自动释放时间，默认 10 秒 */
    long leaseTime() default -1;
    
    /** 时间单位 */
    TimeUnit unit() default TimeUnit.SECONDS;
    
    /** 锁获取失败的提示信息 */
    String msg() default "系统繁忙，请勿重复提交";
}