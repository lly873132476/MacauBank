package com.macau.bank.forex.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.forex.common.enums.ForexTradeStatusEnum;
import com.macau.bank.forex.domain.entity.ForexTradeOrder;
import com.macau.bank.forex.domain.repository.ForexTradeOrderRepository;
import com.macau.bank.forex.infrastructure.mapper.ForexTradeOrderMapper;
import com.macau.bank.forex.infrastructure.persistent.converter.ForexTradeOrderConverter;
import com.macau.bank.forex.infrastructure.persistent.po.ForexTradeOrderPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class ForexTradeOrderRepositoryImpl implements ForexTradeOrderRepository {

    @Resource
    private ForexTradeOrderMapper forexTradeOrderMapper;

    @Resource
    private ForexTradeOrderConverter forexTradeOrderConverter;

    @Override
    public ForexTradeOrder findByRequestId(String requestId) {
        ForexTradeOrderPO po = forexTradeOrderMapper.selectOne(
                new LambdaQueryWrapper<ForexTradeOrderPO>().eq(ForexTradeOrderPO::getRequestId, requestId)
        );
        return forexTradeOrderConverter.toEntity(po);
    }

    @Override
    public void save(ForexTradeOrder order) {
        ForexTradeOrderPO po = forexTradeOrderConverter.toPO(order);
        if (po.getId() == null) {
            forexTradeOrderMapper.insert(po);
            order.setId(po.getId());
        } else {
            forexTradeOrderMapper.updateById(po);
        }
    }

    @Override
    public void updateStatus(String txnId, ForexTradeStatusEnum status, String failReason) {
        ForexTradeOrderPO update = new ForexTradeOrderPO();
        update.setStatus(status);
        update.setFailReason(failReason);
        update.setUpdateTime(LocalDateTime.now());
        
        forexTradeOrderMapper.update(update, new LambdaQueryWrapper<ForexTradeOrderPO>()
                .eq(ForexTradeOrderPO::getTxnId, txnId));
    }

    @Override
    public void update(ForexTradeOrder order) {
        ForexTradeOrderPO po = forexTradeOrderConverter.toPO(order);
        forexTradeOrderMapper.updateById(po);
    }
}
