package com.macau.bank.account.domain.repository;

import com.macau.bank.account.domain.entity.AccountFreezeLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountFreezeLogRepository {
    void save(AccountFreezeLog freezeLog);
    AccountFreezeLog findByFlowNo(String flowNo);

    List<AccountFreezeLog> findDeadLogs(LocalDateTime beforeTime, int limit);
}
