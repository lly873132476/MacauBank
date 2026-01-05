package com.macau.bank.common.framework.web.config;

import com.macau.bank.common.framework.web.interceptor.AuthInterceptor;
import com.macau.bank.common.framework.web.interceptor.HeaderContextInterceptor;
import com.macau.bank.common.framework.web.resolver.ContextArgumentResolver;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CommonWebConfig implements WebMvcConfigurer {

    @Resource
    private HeaderContextInterceptor headerContextInterceptor;
    @Resource
    private AuthInterceptor authInterceptor;

    @Resource
    private ContextArgumentResolver contextArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 基础设施拦截器：拦截所有路径 ( /** )
        // 不管是不是登录，先把 TraceId 生成了，日志上下文准备好
        registry.addInterceptor(headerContextInterceptor)
                .addPathPatterns("/**")
                .order(0);

        // 2. 业务认证拦截器：排除登录注册 ( excludePathPatterns )
        // 只有需要鉴权的接口，才走这一步
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/public/**",
                        "/auth/token/verify",
                        "/admin/**",
                        "/account/admin/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/swagger-ui/**",
                        "/mock/**"
                ) // 配置化排除
                .order(1);
    }

    /**
     * 添加参数解析器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(contextArgumentResolver);
    }
}