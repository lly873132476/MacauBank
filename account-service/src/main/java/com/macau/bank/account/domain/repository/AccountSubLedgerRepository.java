package com.macau.bank.account.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.macau.bank.account.domain.entity.AccountSubLedger;
import java.time.LocalDate;

public interface AccountSubLedgerRepository {
    void save(AccountSubLedger subLedger);
    AccountSubLedger findByRequestId(String requestId);
    
    // 复杂查询保留 Page 参数，或者重构为 query object
    IPage<AccountSubLedger> page(String userNo, String accountNo, String currencyCode, 
                                 LocalDate startDate, LocalDate endDate, 
                                 com.macau.bank.common.core.enums.FlowDirection direction, 
                                 com.macau.bank.common.core.enums.BizType bizType,
                                 int page, int pageSize);
}
