package com.macau.bank.common.framework.web.config;

import com.macau.bank.common.framework.web.advice.GlobalRequestBodyAdvice;
import com.macau.bank.common.framework.web.handler.GlobalExceptionHandler;
import com.macau.bank.common.framework.web.interceptor.AuthInterceptor;
import com.macau.bank.common.framework.web.interceptor.HeaderContextInterceptor;
import com.macau.bank.common.framework.web.resolver.ContextArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Common模块的自动装配入口
 * 作用：像一个清单一样，告诉Spring Boot这个模块里有哪些Bean需要加载
 */
@Configuration
@Import({

    // 1. Web相关配置 (里面包含了 Interceptor 的注册逻辑)
    CommonWebConfig.class,

    // 2. 注册参数增强
    GlobalRequestBodyAdvice.class,

    // 3. 全局异常处理器
    GlobalExceptionHandler.class,

    // 4. 注册 TraceId 上下文拦截器 (变为 Bean)
    HeaderContextInterceptor.class,

    // 5. 注册 认证拦截器 (变为 Bean，从而激活 @Resource)
    AuthInterceptor.class,

    // 6. 当前用户参数解析器
    ContextArgumentResolver.class
    
})
public class CommonAutoConfiguration {
    // 这里也可以定义一些 @Bean
}