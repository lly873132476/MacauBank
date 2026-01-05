package com.macau.bank.common.framework.web.interceptor;

import com.macau.bank.common.core.constant.CommonConstant;
import com.macau.bank.common.core.util.RedisUtil;
import com.macau.bank.common.framework.web.context.RequestHeaderContext;
import com.macau.bank.common.framework.web.model.BaseRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

public class HeaderContextInterceptor implements HandlerInterceptor {

    @Resource
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 准备数据容器
        BaseRequest baseInfo = new BaseRequest();

        // 2. 直接获取 SkyWalking 的 TraceId
        // 如果没挂 Agent，这里会返回 "Ignored_Trace" 或空字符串，我们可以做个兼容
        String traceId = TraceContext.traceId();

        // 兼容逻辑：如果本地开发没挂 Agent，为了不报错，可以降级生成一个
        if (!StringUtils.hasText(traceId) || "Ignored_Trace".equals(traceId)) {
            // 尝试从 Header 取（可能是上游传过来的）
            traceId = request.getHeader(CommonConstant.TRACE_ID_HEADER);
            if (!StringUtils.hasText(traceId)) {
                traceId = UUID.randomUUID().toString().replace("-", ""); // 只有没 Agent 时才手动生成
            }
        }

        baseInfo.setTraceId(traceId);
        
        // 关键：放入日志 MDC，这样 log.info() 会自动带上 traceId
        MDC.put(CommonConstant.LOG_TRACE_ID, traceId);

        // 3. 提取其他 Header
        baseInfo.setDeviceId(request.getHeader(CommonConstant.DEVICE_ID_HEADER));
        baseInfo.setAppVersion(request.getHeader(CommonConstant.APP_VERSION_HEADER));
        baseInfo.setClientIp(getClientIp(request));
        
        // 4. 解析 Token 获取 userNo (这里简单模拟，实际可能要调鉴权服务或解析JWT)
        String token = request.getHeader(CommonConstant.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(token)) {
            String redisKey = CommonConstant.REDIS_TOKEN_PREFIX + token;
            String userNo = redisUtil.get(redisKey);
            baseInfo.setUserNo(userNo);
        }

        // 5. 放入 ThreadLocal 上下文
        RequestHeaderContext.set(baseInfo);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理，防止内存泄漏和线程污染
        RequestHeaderContext.clear();
        MDC.clear();
    }

    private String getClientIp(HttpServletRequest request) {
        // 省略具体IP获取逻辑...
        return request.getRemoteAddr();
    }
}