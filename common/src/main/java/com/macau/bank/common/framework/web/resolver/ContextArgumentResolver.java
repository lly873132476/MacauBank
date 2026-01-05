package com.macau.bank.common.framework.web.resolver; // 建议移动包路径

import com.macau.bank.common.framework.web.annotation.CurrentUser;
import com.macau.bank.common.framework.web.context.RequestHeaderContext; // 引入上下文
import com.macau.bank.common.framework.web.model.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 当前用户参数解析器
 * 适配 RequestHeaderContext 体系
 */
@Slf4j
@Component
public class ContextArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 只要参数类型是 BaseRequest 或者是 String 且带 @CurrentUser，都处理
        return BaseRequest.class.isAssignableFrom(parameter.getParameterType())
                || parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // 1. 拿数据
        BaseRequest context = RequestHeaderContext.get();
        if (context == null) context = new BaseRequest(); // 防空

        // 2. 如果参数是 BaseRequest 类型（万能模式）
        if (BaseRequest.class.isAssignableFrom(parameter.getParameterType())) {
            return context;
        }

        // 3. 如果参数是 String 且有 @CurrentUser（经典模式）
        if (parameter.hasParameterAnnotation(CurrentUser.class)) {
            return context.getUserNo();
        }

        return null;
    }
}