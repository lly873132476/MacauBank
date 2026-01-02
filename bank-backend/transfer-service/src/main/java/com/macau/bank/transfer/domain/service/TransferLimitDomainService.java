package com.macau.bank.transfer.domain.service;

import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.transfer.domain.entity.TransferLimitConfig;
import com.macau.bank.transfer.domain.repository.TransferLimitConfigRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 转账限额领域服务
 */
@Service
public class TransferLimitDomainService {

    @Resource
    private TransferLimitConfigRepository transferLimitConfigRepository;

    /**
     * 获取限额配置
     */
    public TransferLimitConfig getLimitConfig(String userLevel, String transferTypeStr, String currency) {
        // String -> Enum
        TransferType transferType = TransferType.valueOf(transferTypeStr);
        return transferLimitConfigRepository.findMatch(userLevel, transferType, currency);
    }

    /**
     * 校验单笔限额
     */
    public boolean checkSingleLimit(String userLevel, String transferType, String currency, BigDecimal amount) {
        TransferLimitConfig config = getLimitConfig(userLevel, transferType, currency);
        if (config == null) return true; // 无配置不限制
        return amount.compareTo(config.getSingleLimit()) <= 0;
    }
}