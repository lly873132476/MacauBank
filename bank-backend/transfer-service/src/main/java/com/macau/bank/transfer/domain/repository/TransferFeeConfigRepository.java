package com.macau.bank.transfer.domain.repository;

import com.macau.bank.transfer.domain.entity.TransferFeeConfig;
import java.util.List;

public interface TransferFeeConfigRepository {
    boolean save(TransferFeeConfig feeConfig);
    TransferFeeConfig findById(Integer id);
    List<TransferFeeConfig> findAll();
    
    /**
     * 查找匹配的费率配置
     * @param channel 渠道
     * @param currency 币种
     * @param userLevel 用户等级
     * @return 配置
     */
    TransferFeeConfig findMatch(String channel, String currency, String userLevel);
}
