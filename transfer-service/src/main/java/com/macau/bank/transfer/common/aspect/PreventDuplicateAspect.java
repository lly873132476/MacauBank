package com.macau.bank.transfer.common.aspect;

import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.transfer.common.annotation.PreventDuplicate;
import com.macau.bank.transfer.common.result.TransferErrorCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 防重复提交切面
 * 
 * <p>
 * 使用 Redis Token 实现幂等性
 * </p>
 * 
 * @author MacauBank
 */
@Slf4j
@Aspect
@Component
public class PreventDuplicateAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(com.macau.bank.transfer.common.annotation.PreventDuplicate)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PreventDuplicate annotation = method.getAnnotation(PreventDuplicate.class);

        // 2. 获取 requestId（从请求头或请求参数）
        String requestId = getRequestId();
        if (requestId == null || requestId.isEmpty()) {
            log.warn("requestId 为空，跳过防重检查");
            return joinPoint.proceed();
        }

        // 3. 构建 Redis Key
        String key = annotation.prefix() + requestId;

        // 4. 尝试设置 Token（原子操作）
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", annotation.timeout(), annotation.timeUnit());

        if (Boolean.FALSE.equals(success)) {
            // Token 已存在，说明是重复请求
            log.warn("检测到重复提交，requestId: {}", requestId);
            throw new BusinessException(TransferErrorCode.DUPLICATE_REQUEST);
        }

        try {
            // 5. 执行业务逻辑
            return joinPoint.proceed();
        } catch (Exception e) {
            // 6. 如果业务逻辑失败，删除 Token（允许重试）
            stringRedisTemplate.delete(key);
            throw e;
        }
    }

    /**
     * 获取 requestId
     * 
     * <p>
     * 优先从请求头获取，其次从请求参数获取
     * </p>
     */
    private String getRequestId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();

        // 1. 从请求头获取
        String requestId = request.getHeader("X-Request-Id");
        if (requestId != null && !requestId.isEmpty()) {
            return requestId;
        }

        // 2. 从请求参数获取
        requestId = request.getParameter("requestId");
        if (requestId != null && !requestId.isEmpty()) {
            return requestId;
        }

        // 3. 从请求体获取（需要解析 JSON，这里简化处理）
        // 实际项目中可以通过 @RequestBody 参数获取
        return null;
    }
}
