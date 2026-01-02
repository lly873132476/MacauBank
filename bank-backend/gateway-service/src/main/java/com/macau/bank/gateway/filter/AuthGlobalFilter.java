package com.macau.bank.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macau.bank.common.core.constant.CommonConstant;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 网关全局鉴权过滤器
 * 核心逻辑：
 * 1. 白名单放行 (登录/注册)
 * 2. 校验 Token 是否存在且有效 (查 Redis)
 * 3. 提取 Token 中的 userNo，放入请求头传给下游
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 路径匹配器
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 假设这些路径不需要鉴权 (可以在配置文件里配)
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/code/**", // 验证码
            "/doc.html",     // Swagger
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/admin/**",
            "/account/admin/**",
            "/mock/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 0. 🔥【新增】如果是 OPTIONS 请求，直接放行 (CORS 预检)
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 1. 白名单放行 (保持不变)
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        // 2. 获取 Token (保持不变)
        String token = request.getHeaders().getFirst(CommonConstant.AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(token)) {
            return buildErrorResponse(exchange, ResultCode.TOKEN_INVALID.getCode(), "未携带认证 Token");
        }

        // 3. 查 Redis 校验 Token
        String redisKey = CommonConstant.REDIS_TOKEN_PREFIX + token;

        return redisTemplate.opsForValue().get(redisKey)
                // 【修改点】 switchIfEmpty 必须放在 flatMap 之前！
                // 只有当 Redis 查不到数据(Empty)时，才执行这里的逻辑
                .switchIfEmpty(Mono.defer(() -> {
                    // 因为 switchIfEmpty 需要返回 Mono<String> 来匹配流类型，
                    // 而 buildErrorResponse 返回 Mono<Void>，所以这里要转一下
                    return buildErrorResponse(exchange,ResultCode.TOKEN_INVALID.getCode(), "Token 已过期或不存在")
                            .then(Mono.empty());
                }))
                .flatMap(redisValue -> {
                    // 能进到这里，说明 switchIfEmpty 没有执行，redisValue 一定有值
                    try {
                        String userNo = redisValue;

                        // 4. 把 userNo 塞进 Header
                        ServerHttpRequest newRequest = request.mutate()
                                .header(CommonConstant.USER_NO_HEADER, userNo)
                                .build();

                        // 5. 放行
                        return chain.filter(exchange.mutate().request(newRequest).build());

                    } catch (Exception e) {
                        log.error("Token 解析失败", e);
                        return buildErrorResponse(exchange, 401, "Token 无效或已损坏");
                    }
                });
    }

    /**
     * 辅助方法：构建 JSON 格式的错误响应
     */
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, int code, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().put("Content-Type", Collections.singletonList(MediaType.APPLICATION_JSON.toString()));

        // 使用 Common 模块的 Result 对象
        Result<Object> result = Result.fail(code, msg);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 优先级在 TraceIdFilter (Integer.MIN_VALUE) 之后
        // 但在 NettyRoutingFilter 之前
        return -100; 
    }
}