package com.macau.bank.common.sequence; // 建议放在 common 或 domain 模块

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

/**
 * 银行卡号生成器 (基于 ISO 7812 标准 + Luhn 算法)
 */
@Component
public class CardNumberGenerator {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 假设 Macau Bank 的银联 BIN 号
    private static final String BIN_PREFIX = "628888";
    
    // Redis Key
    private static final String REDIS_KEY_CARD_SEQ = "macau:bank:seq:card";

    /**
     * 生成唯一的 16 位银行卡号
     */
    public String nextCardNumber() {
        // 1. 获取 9 位序列号 (Redis原子递增)
        // 这里的 9 位决定了最大发卡量，真实银行会更复杂，Demo 足够了
        Long seq = stringRedisTemplate.opsForValue().increment(REDIS_KEY_CARD_SEQ);
        
        // 补齐 9 位，例如 1 -> "000000001"
        String sequenceBody = String.format("%09d", seq);
        
        // 2. 拼装前 15 位 (BIN + 序列)
        String prefix15 = BIN_PREFIX + sequenceBody;
        
        // 3. 计算最后一位校验码 (Luhn 算法)
        int checkDigit = calculateLuhnCheckDigit(prefix15);
        
        // 4. 返回完整 16 位卡号
        return prefix15 + checkDigit;
    }

    /**
     * Luhn 算法 (模10算法) 计算校验位
     * 逻辑：
     * 1. 从右向左，奇数位不变，偶数位 * 2。
     * 2. 如果 * 2 后大于 9，则减 9 (或者 个位+十位)。
     * 3. 所有结果相加。
     * 4. 校验位 = (10 - (sum % 10)) % 10
     */
    private int calculateLuhnCheckDigit(String cardNoPrefix) {
        int sum = 0;
        boolean alternate = true; // 从最右边开始，第一个数字（其实是倒数第二位）需要 x2
        
        // 从字符串最后一位往前遍历
        for (int i = cardNoPrefix.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNoPrefix.substring(i, i + 1));
            
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1; // 等同于 n - 9
                }
            }
            sum += n;
            alternate = !alternate; // 切换标志
        }
        
        // 计算校验位
        int remainder = sum % 10;
        return (remainder == 0) ? 0 : (10 - remainder);
    }
}