package com.macau.bank.account.domain.repository;

import com.macau.bank.account.domain.entity.AccountInfo;
import java.util.List;

public interface AccountInfoRepository {
    void save(AccountInfo accountInfo);
    AccountInfo findById(Long id);
    AccountInfo findByAccountNo(String accountNo);
    AccountInfo findByCardNumber(String cardNumber);
    List<AccountInfo> findByUserNo(String userNo);
}
