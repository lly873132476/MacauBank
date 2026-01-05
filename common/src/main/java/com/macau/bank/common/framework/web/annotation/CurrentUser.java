package com.macau.bank.common.framework.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当前登录用户注解
 * 
 * 用于Controller方法参数，自动注入当前登录用户的userNo
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @GetMapping("/profile")
 * public Result<UserInfo> getProfile(@CurrentUser String userNo) {
 *     return userService.getByUserNo(userNo);
 * }
 * }
 * </pre>
 * 
 * 注意：使用此注解的接口必须配置认证拦截器
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    
    /**
     * 是否必须（默认true）
     * 如果为true且userNo为空，会抛出异常
     * 如果为false且userNo为空，返回null
     */
    boolean required() default true;
}
