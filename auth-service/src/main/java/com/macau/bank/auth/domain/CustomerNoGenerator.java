package com.macau.bank.auth.domain;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerNoGenerator {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 个人客户前缀 (C for Customer, or just 6)
    private static final String CIF_PREFIX = "6"; 
    
    // Redis Key
    private static final String REDIS_KEY_CIF_SEQ = "macau:bank:seq:cif";

    /**
     * 生成 8 位客户号 (CIF Number)
     * 格式: 6 + 7位序列 (千万级容量)
     * 例如: 60000001
     */
    public String nextCustomerNo() {
        // 1. 原子递增
        Long seq = stringRedisTemplate.opsForValue().increment(REDIS_KEY_CIF_SEQ);
        
        // 2. 补齐 7 位
        return CIF_PREFIX + String.format("%07d", seq);
    }
}