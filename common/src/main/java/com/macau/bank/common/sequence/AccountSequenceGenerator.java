package com.macau.bank.common.sequence;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 序列号生成器 - 这是一个“组件”，不是“工具类”
 */
@Component 
public class AccountSequenceGenerator {

    // 澳门银行机构代码 (假设)
    private static final String BANK_CODE = "888";

    // 总行营业部代码
    private static final String BRANCH_CODE = "001";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_KEY_PREFIX = "macau:bank:seq:account";

    /**
     * 生成唯一的 16 位账号
     */
    public String nextAccountNo() {
        // 1. Redis 原子递增
        Long seq = stringRedisTemplate.opsForValue().increment(REDIS_KEY_PREFIX);
        
        // 2. 补零逻辑
        // 假设机构号 888，分行号由参数传入
        return BANK_CODE + BRANCH_CODE + String.format("%010d", seq);
    }
}