package com.macau.bank.common.framework.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.macau.bank.common.core.constant.CommonConstant;
import com.macau.bank.common.core.result.IResultCode;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.common.core.result.ResultCode;
import com.macau.bank.common.core.util.RedisUtil;
import com.macau.bank.common.framework.web.context.RequestHeaderContext;
import com.macau.bank.common.framework.web.model.BaseRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 通用认证拦截器
 * 职责：
 * 1. 强校验 Token 有效性 (Redis)
 * 2. 将解析出的 UserNO 注入到 RequestHeaderContext
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 基础环境检查
        if (!redisUtil.isAvailable()) {
            log.error("认证失败: Redis服务不可用");
            writeErrorResponse(response, ResultCode.FAIL, "系统繁忙，请稍后重试");
            return false;
        }

        // 2. 获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            // 对于非强制登录接口，这里可以放行，具体看业务策略
            // 这里假设是强制登录
            log.warn("请求未携带Token: {}", request.getRequestURI());
            writeErrorResponse(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        // 3. 验证Token (Redis)
        try {
            String redisKey = CommonConstant.REDIS_TOKEN_PREFIX + token;
            String userNo = redisUtil.get(redisKey);

            if (userNo == null) {
                log.warn("Token无效或已过期: {}", token);
                writeErrorResponse(response, ResultCode.TOKEN_INVALID);
                return false;
            }

            // ================== 核心修改点 START ==================

            // 4. 获取当前线程的上下文 (由 HeaderContextInterceptor 预先初始化)
            BaseRequest context = RequestHeaderContext.get();

            // 防御性编程：虽然理应由上一个拦截器初始化，但以防万一为空，手动补救
            if (context == null) {
                context = new BaseRequest();
                RequestHeaderContext.set(context);
            }

            // 5. 注入 userNo 到上下文 (连接了之前的 GlobalRequestBodyAdvice)
            context.setUserNo(userNo);

            // 6. 放入 MDC (让日志自动打印 userNo，方便排查是谁在操作)
            MDC.put(CommonConstant.LOG_USER_NO, userNo);
            // 7. SkyWalking打标
            ActiveSpan.tag("userNo", userNo);

            // ================== 核心修改点 END ====================

            log.debug("Token验证成功: userNo={}", userNo);
            return true;

        } catch (Exception e) {
            log.error("Token验证失败: 系统异常", e);
            writeErrorResponse(response, ResultCode.FAIL, "认证服务异常");
            return false;
        }
    }

    // ... writeErrorResponse 方法保持不变 ...
    private void writeErrorResponse(HttpServletResponse response, IResultCode resultCode) throws Exception {
        writeErrorResponse(response, resultCode, resultCode.getMessage());
    }

    private void writeErrorResponse(HttpServletResponse response, IResultCode resultCode, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        Result<Void> result = Result.fail(resultCode.getCode(), message);
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}