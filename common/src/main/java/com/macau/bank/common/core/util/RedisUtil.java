package com.macau.bank.common.core.util;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 (基于 Spring Data Redis 重构)
 * 不再手动管理 JedisPool，完全托管给 Spring Boot
 */
@Slf4j
@Component
public class RedisUtil {

    // 注入 Spring 自动配置好的 StringRedisTemplate
    // 专门用于处理 Key 和 Value 都是 String 的场景
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 检查Redis是否可用
     * 原理：尝试获取底层连接并执行 ping
     */
    public boolean isAvailable() {
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            log.error("Redis服务当前不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 设置字符串值
     */
    public void set(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis set error: key={}", key, e);
            throw e; // 或者根据业务决定是否抛出
        }
    }

    /**
     * 设置字符串值（带过期时间，单位：秒）
     * 对应原来的 setex
     */
    public void setex(String key, int seconds, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis setex error: key={}", key, e);
            throw e;
        }
    }

    /**
     * 获取字符串值
     */
    public String get(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get error: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除键
     */
    public void del(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis del error: key={}", key, e);
        }
    }

    /**
     * 判断键是否存在
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis exists error: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public void expire(String key, int seconds) {
        try {
            stringRedisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis expire error: key={}", key, e);
        }
    }
}