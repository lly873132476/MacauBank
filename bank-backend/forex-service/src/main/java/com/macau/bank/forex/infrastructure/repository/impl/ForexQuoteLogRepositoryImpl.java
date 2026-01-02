package com.macau.bank.forex.infrastructure.repository.impl;

import com.macau.bank.forex.domain.entity.ForexQuoteLog;
import com.macau.bank.forex.domain.repository.ForexQuoteLogRepository;
import com.macau.bank.forex.infrastructure.mapper.ForexQuoteLogMapper;
import com.macau.bank.forex.infrastructure.persistent.converter.ForexQuoteLogConverter;
import com.macau.bank.forex.infrastructure.persistent.po.ForexQuoteLogPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class ForexQuoteLogRepositoryImpl implements ForexQuoteLogRepository {

    @Resource
    private ForexQuoteLogMapper forexQuoteLogMapper;

    @Resource
    private ForexQuoteLogConverter forexQuoteLogConverter;

    @Override
    public void save(ForexQuoteLog log) {
        ForexQuoteLogPO po = forexQuoteLogConverter.toPO(log);
        forexQuoteLogMapper.insert(po);
        log.setId(po.getId());
    }
}
