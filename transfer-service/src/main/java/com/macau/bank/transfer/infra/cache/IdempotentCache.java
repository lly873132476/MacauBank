package com.macau.bank.transfer.infra.cache;

import com.macau.bank.common.core.util.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 幂等性缓存（Redis实现）
 * 用于防止重复的转账请求
 */
@Slf4j
@Component
public class IdempotentCache {

    @Resource
    private RedisUtil redisUtil; // Autowire RedisUtil

    private static final String KEY_PREFIX = "idempotent:";
    private static final int PROCESSING_EXPIRE_SECONDS = 60; // 处理中状态过期时间（1分钟）
    private static final int SUCCESS_EXPIRE_SECONDS = 86400; // 成功状态过期时间（24小时）

    /**
     * 尝试锁定（标记为处理中）
     * 利用 setnx 原子性
     *
     * @param idempotentKey 幂等键
     * @return true=锁定成功(可以执行), false=锁定失败(重复请求或正在处理)
     */
    public boolean tryLock(String idempotentKey) {
        String redisKey = KEY_PREFIX + idempotentKey;
        
        String value = redisUtil.get(redisKey);
        if (value != null) {
            log.warn("幂等性检查 - 请求重复或正在处理: key={}, status={}", idempotentKey, value);
            return false;
        }
        
        redisUtil.setex(redisKey, PROCESSING_EXPIRE_SECONDS, "PROCESSING");
        return true;
    }

    /**
     * 标记为处理成功
     *
     * @param idempotentKey 幂等键
     * @param transactionId 交易流水号（可选，存入value方便查询）
     */
    public void markAsSuccess(String idempotentKey, Long transactionId) {
        String redisKey = KEY_PREFIX + idempotentKey;
        String value = "SUCCESS:" + transactionId;
        redisUtil.setex(redisKey, SUCCESS_EXPIRE_SECONDS, value);
        log.info("幂等性检查 - 标记为成功: key={}", idempotentKey);
    }

    /**
     * 标记为处理失败（删除key，允许重试）
     *
     * @param idempotentKey 幂等键
     */
    public void markAsFailed(String idempotentKey) {
        String redisKey = KEY_PREFIX + idempotentKey;
        redisUtil.del(redisKey);
        log.info("幂等性检查 - 标记为失败(已清除): key={}", idempotentKey);
    }
    
    /**
     * 检查是否已处理成功
     */
    public boolean isSuccess(String idempotentKey) {
        String redisKey = KEY_PREFIX + idempotentKey;
        String value = redisUtil.get(redisKey);
        return value != null && value.startsWith("SUCCESS");
    }
}
