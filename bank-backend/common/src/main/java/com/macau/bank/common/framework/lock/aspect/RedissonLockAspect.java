package com.macau.bank.common.framework.lock.aspect;

import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.framework.lock.annotation.RedissonLock;
import com.macau.bank.common.framework.lock.config.MacauLockProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class RedissonLockAspect {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MacauLockProperties macauLockProperties;
    
    private final ExpressionParser parser = new SpelExpressionParser();
    // 建议用接口接收，通用性更强
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(redissonLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        // 1. 解析 SpEL 获取 Key (例如根据 userNo 加锁)
        String key = parseKey(redissonLock.key(), joinPoint);

        // 2. 决策时间参数
        // 如果注解写了 -1，就用全局配置；否则用注解里写的特殊值
        long waitTime = redissonLock.waitTime() == -1 ? macauLockProperties.getWaitTime() : redissonLock.waitTime();
        long leaseTime = redissonLock.leaseTime() == -1 ? macauLockProperties.getLeaseTime() : redissonLock.leaseTime();
        
        // 3. 获取锁对象
        RLock lock = redissonClient.getLock(key);
        boolean isLocked = false;
        
        try {
            // 4. 尝试加锁
            isLocked = lock.tryLock(waitTime, leaseTime, redissonLock.unit());
            if (isLocked) {
                // 加锁成功，执行业务逻辑
                return joinPoint.proceed();
            } else {
                // 加锁失败
                throw new BusinessException(redissonLock.msg());
            }
        } finally {
            // 4. 释放锁（只有拥有锁的线程才能释放）
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 解析 SpEL 表达式
     */
    private String parseKey(String key, ProceedingJoinPoint joinPoint) {
        if (!StringUtils.hasText(key)) return "default-lock";
        // 如果不是 SpEL 表达式直接返回
        if (!key.contains("#")) return key;
        
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String[] paramNames = discoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        return parser.parseExpression(key).getValue(context, String.class);
    }
}