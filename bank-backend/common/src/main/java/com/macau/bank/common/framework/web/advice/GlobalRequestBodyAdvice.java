package com.macau.bank.common.framework.web.advice;

import com.macau.bank.common.framework.web.context.RequestHeaderContext;
import com.macau.bank.common.framework.web.model.BaseRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

/**
 * 自动填充 @RequestBody 中的 BaseRequest 字段
 */
@RestControllerAdvice
public class GlobalRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, 
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // 只有继承了 BaseRequest 的类才进行增强
        try {
            return BaseRequest.class.isAssignableFrom(Class.forName(targetType.getTypeName()));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, 
                                MethodParameter parameter, Type targetType, 
                                Class<? extends HttpMessageConverter<?>> converterType) {
        
        if (body instanceof BaseRequest) {
            BaseRequest requestParam = (BaseRequest) body;
            // 从 ThreadLocal 获取拦截器准备好的数据
            BaseRequest contextInfo = RequestHeaderContext.get();

            if (contextInfo != null) {
                requestParam.setTraceId(contextInfo.getTraceId());
                requestParam.setDeviceId(contextInfo.getDeviceId());
                requestParam.setAppVersion(contextInfo.getAppVersion());
                requestParam.setClientIp(contextInfo.getClientIp());
                requestParam.setUserNo(contextInfo.getUserNo());
            }
        }
        return body;
    }
}