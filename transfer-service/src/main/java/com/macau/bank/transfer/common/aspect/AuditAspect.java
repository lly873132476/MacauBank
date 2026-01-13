package com.macau.bank.transfer.common.aspect;

import cn.hutool.json.JSONUtil;
import com.macau.bank.transfer.common.annotation.Auditable;
import com.macau.bank.transfer.domain.repository.AuditLogRepository;
import com.macau.bank.transfer.domain.entity.AuditLog;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 审计日志切面
 * <p>
 * 拦截所有标记了 @Auditable 注解的方法，自动记录审计日志。
 * <p>
 * 记录内容包括：
 * - 操作人、操作类型、目标对象
 * - 请求参数、响应结果
 * - 操作耗时、客户端IP
 * - 异常信息（如果失败）
 * <p>
 * 架构说明：
 * - 遵循 DDD 分层，通过 Repository 接口访问持久层
 * - 不直接依赖 Mapper，保持层间解耦
 */
@Slf4j
@Aspect
@Component
public class AuditAspect {

    @Resource
    private AuditLogRepository auditLogRepository;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 构建审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setTraceId(getTraceId());
        auditLog.setUserId(getCurrentUserId());
        auditLog.setAction(auditable.action());
        auditLog.setTargetType(auditable.targetType());
        auditLog.setCreateTime(LocalDateTime.now());

        // 解析目标ID
        if (!auditable.targetIdExpr().isEmpty()) {
            String targetId = parseSpelExpression(auditable.targetIdExpr(), pjp);
            auditLog.setTargetId(targetId);
        }

        // 记录请求参数
        if (auditable.logRequest()) {
            Object[] args = pjp.getArgs();
            if (args != null && args.length > 0) {
                auditLog.setAfterData(toJson(args[0]));
            }
        }

        // 获取客户端信息
        fillClientInfo(auditLog);

        Object result = null;
        try {
            // 执行业务逻辑
            result = pjp.proceed();

            // 记录成功结果
            auditLog.setResult("SUCCESS");
            if (auditable.logResponse() && result != null) {
                // 避免响应数据过大，截断处理
                String responseJson = toJson(result);
                if (responseJson.length() > 2000) {
                    responseJson = responseJson.substring(0, 2000) + "...(truncated)";
                }
                auditLog.setAfterData(responseJson);
            }

            return result;
        } catch (Throwable e) {
            // 记录失败信息
            auditLog.setResult("FAILURE");
            auditLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            // 记录耗时并异步保存
            auditLog.setDuration(System.currentTimeMillis() - startTime);
            saveAuditLog(auditLog);
        }
    }

    /**
     * 解析 SpEL 表达式
     */
    private String parseSpelExpression(String spelExpr, ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = nameDiscoverer.getParameterNames(method);
            Object[] args = pjp.getArgs();

            EvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            Expression expression = parser.parseExpression(spelExpr);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("解析 SpEL 表达式失败: {}", spelExpr, e);
            return null;
        }
    }

    /**
     * 获取链路追踪ID
     */
    private String getTraceId() {
        // 优先从请求头获取（如果有 SkyWalking 等链路追踪）
        HttpServletRequest request = getRequest();
        if (request != null) {
            String traceId = request.getHeader("X-Trace-Id");
            if (traceId != null) {
                return traceId;
            }
        }
        // 否则生成一个
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            // 从请求头获取（Gateway 传递过来的）
            String userId = request.getHeader("X-User-Id");
            if (userId != null) {
                return userId;
            }
        }
        return "SYSTEM";
    }

    /**
     * 填充客户端信息
     */
    private void fillClientInfo(AuditLog auditLog) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            auditLog.setClientIp(getClientIp(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取 HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * 对象转 JSON
     */
    private String toJson(Object obj) {
        try {
            return JSONUtil.toJsonStr(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    /**
     * 保存审计日志（通过 Repository）
     */
    private void saveAuditLog(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }
}
