package com.macau.bank.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macau.bank.common.core.constant.CommonConstant;
import com.macau.bank.common.core.result.Result; // 1. 引入 Common 的 Result
import com.macau.bank.gateway.common.result.GatewayErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.MessageSource; // 2. 引入国际化
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Map;

/**
 * 网关全局异常处理 (进阶版)
 */
@Slf4j
@Order(-1)
@Configuration
@RequiredArgsConstructor
public class GlobalErrorExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource; // 注入消息源

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 1. 设置 JSON 响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 2. 获取请求的 TraceId (Gateway 自动生成的)
        String traceId = exchange.getRequest().getId();

        // 3. 确定状态码和错误信息
        int code;
        String msg;

        // 获取当前请求的语言环境 (zh_CN, en_US)
        Locale locale = exchange.getLocaleContext().getLocale();
        if (locale == null) {
            locale = Locale.getDefault();
        }

        if (ex instanceof ResponseStatusException) {
            // Spring Cloud Gateway 抛出的标准异常 (如 404 Not Found)
            ResponseStatusException statusEx = (ResponseStatusException) ex;
            response.setStatusCode(statusEx.getStatusCode());
            code = statusEx.getStatusCode().value();
            // 尝试去 i18n 文件里找对应翻译，找不到就用异常原话
            msg = statusEx.getReason();
        } else {
            // 其他未知道异常 (500)
            log.error("[Gateway Error] TraceId: {}, Error: {}", traceId, ex.getMessage(), ex);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            code = HttpStatus.INTERNAL_SERVER_ERROR.value();
            // 从 i18n 资源文件读取 "system.error"
            // 确保你的 messages_gateway_zh_CN.properties 里有 system.error=系统繁忙，请稍后重试
            try {
                msg = messageSource.getMessage(GatewayErrorCode.GATEWAY_ERROR.getI18nKey(), null, GatewayErrorCode.GATEWAY_ERROR.getMessage(), locale);
            } catch (Exception e) {
                msg = GatewayErrorCode.SERVICE_UNAVAILABLE.getMessage();
            }
        }

        // 4. 构建统一返回对象 (复用 Common 模块)
        // 假设 Common Result 有 setTraceId 方法，或者你可以把 traceId 放在 data 里
        Result<Object> result = Result.fail(code, msg);
        // 如果 Result 类没字段放 traceId，可以暂时不放，或者在 data 里 new 一个 Map 放进去
        result.setData(Map.of(CommonConstant.LOG_TRACE_ID, traceId));

        // 5. 序列化并写入
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(result));
            } catch (JsonProcessingException e) {
                log.error("Error writing response", ex);
                // 降级返回一个最简单的 JSON 字节，防止前端白屏
                return bufferFactory.wrap("{\"code\":500,\"msg\":\"Serialization Error\"}".getBytes());
            }
        }));
    }
}