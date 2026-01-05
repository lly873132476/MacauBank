package com.macau.bank.transfer.domain.repository;

import com.macau.bank.transfer.domain.entity.TransferLimitConfig;
import java.util.List;

public interface TransferLimitConfigRepository {
    boolean save(TransferLimitConfig limitConfig);
    TransferLimitConfig findById(Integer id);
    List<TransferLimitConfig> findAll();
    
    /**
     * 查找限额配置
     */
    TransferLimitConfig findMatch(String userLevel, com.macau.bank.common.core.enums.TransferType transferType, String currency);
}
