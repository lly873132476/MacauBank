package com.macau.bank.transfer.domain.repository;

import com.macau.bank.transfer.domain.entity.BankClearingCode;
import java.util.List;

public interface BankClearingCodeRepository {
    boolean save(BankClearingCode clearingCode);
    BankClearingCode findById(Integer id);
    List<BankClearingCode> findByRegion(String regionCode);
    List<BankClearingCode> findAll();

    /**
     * 条件查询
     */
    List<BankClearingCode> query(BankClearingCode condition);

    /**
     * 查找单个
     */
    BankClearingCode findOne(BankClearingCode condition);
}
