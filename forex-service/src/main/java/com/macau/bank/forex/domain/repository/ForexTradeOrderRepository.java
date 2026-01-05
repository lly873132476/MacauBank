package com.macau.bank.forex.domain.repository;

import com.macau.bank.forex.common.enums.ForexTradeStatusEnum;
import com.macau.bank.forex.domain.entity.ForexTradeOrder;

public interface ForexTradeOrderRepository {
    ForexTradeOrder findByRequestId(String requestId);
    void save(ForexTradeOrder order);
    void updateStatus(String txnId, ForexTradeStatusEnum status, String failReason);
    // 同时也提供全量更新能力，如果需要的话
    void update(ForexTradeOrder order);
}
