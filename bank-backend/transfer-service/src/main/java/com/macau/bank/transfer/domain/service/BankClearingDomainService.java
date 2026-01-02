package com.macau.bank.transfer.domain.service;

import com.macau.bank.transfer.common.enums.ConfigStatusEnum;
import com.macau.bank.transfer.domain.entity.BankClearingCode;
import com.macau.bank.transfer.domain.repository.BankClearingCodeRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 银行清算信息领域服务
 */
@Service
public class BankClearingDomainService {

    @Resource
    private BankClearingCodeRepository bankClearingCodeRepository;

    /**
     * 根据地区查询银行列表
     */
    public List<BankClearingCode> getBanksByRegion(String regionCode) {
        BankClearingCode condition = new BankClearingCode();
        condition.setRegionCode(regionCode);
        condition.setStatus(ConfigStatusEnum.ENABLED);
        return bankClearingCodeRepository.query(condition);
    }

    /**
     * 根据SWIFT查询银行
     */
    public BankClearingCode getBySwiftCode(String swiftCode) {
        BankClearingCode condition = new BankClearingCode();
        condition.setSwiftCode(swiftCode);
        return bankClearingCodeRepository.findOne(condition);
    }
}