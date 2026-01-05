package com.macau.bank.account.domain.repository;

import com.macau.bank.account.domain.entity.AccountBalance;

import java.util.List;

public interface AccountBalanceRepository {

    AccountBalance findByAccountAndCurrency(String accountNo, String currencyCode);

    List<AccountBalance> findByAccountNo(String accountNo);

    List<AccountBalance> findByAccountNos(java.util.List<String> accountNos);

    void save(AccountBalance accountBalance);
}
