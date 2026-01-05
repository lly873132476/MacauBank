package com.macau.bank.account.infra.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
public class AccountSecurityUtil {

    // 在生产环境应从配置中心或 Vault 获取
    @Value("${account.security.hmac-salt:MacauBankDefaultSalt2025}")
    private String salt;

    /**
     * 计算余额校验码
     * HMAC = SHA256(balance + version + salt)
     */
    public String calculateMac(BigDecimal balance, Integer version) {
        if (balance == null || version == null) {
            return null;
        }
        // 格式化金额，保留2位小数，避免精度问题导致hash不一致
        String content = balance.setScale(2, RoundingMode.HALF_UP).toString()
                + "::" + version;
        
        return SecureUtil.hmac(HmacAlgorithm.HmacSHA256, salt.getBytes())
                .digestHex(content);
    }
    
    public boolean verify(BigDecimal balance, Integer version, String macCode) {
        String calculated = calculateMac(balance, version);
        return calculated != null && calculated.equals(macCode);
    }
}
