package com.macau.bank.transfer.domain.service;

import com.macau.bank.transfer.common.enums.FeeCalcModeEnum;
import com.macau.bank.transfer.domain.entity.TransferFeeConfig;
import com.macau.bank.transfer.domain.repository.TransferFeeConfigRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 转账手续费领域服务
 */
@Service
public class TransferFeeDomainService {

    @Resource
    private TransferFeeConfigRepository transferFeeConfigRepository;

    /**
     * 计算手续费
     */
    public BigDecimal calculateFee(String channel, String currency, String userLevel, BigDecimal amount) {
        // 使用 Repository 查询
        TransferFeeConfig config = transferFeeConfigRepository.findMatch(channel, currency, userLevel);

        if (config == null) return BigDecimal.ZERO;

        BigDecimal fee = BigDecimal.ZERO;
        // 1-固定金额 2-百分比 3-固定+百分比
        if (config.getCalcMode() == FeeCalcModeEnum.FIXED) {
            fee = config.getFixedAmount();
        } else if (config.getCalcMode() == FeeCalcModeEnum.PERCENTAGE) {
            fee = amount.multiply(config.getRate());
        } else if (config.getCalcMode() == FeeCalcModeEnum.BOTH) {
            fee = config.getFixedAmount().add(amount.multiply(config.getRate()));
        }

        // 封顶保底
        if (fee.compareTo(config.getMinFee()) < 0) fee = config.getMinFee();
        if (fee.compareTo(config.getMaxFee()) > 0) fee = config.getMaxFee();

        return fee.setScale(2, RoundingMode.HALF_UP);
    }
}