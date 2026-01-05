package com.macau.bank.transfer.infra.repository;

import com.macau.bank.transfer.common.enums.ConfigStatusEnum;
import com.macau.bank.transfer.domain.entity.TransferLimitConfig;
import com.macau.bank.transfer.domain.repository.TransferLimitConfigRepository;
import com.macau.bank.transfer.infra.persistent.converter.TransferLimitConfigPOConverter;
import com.macau.bank.transfer.infra.mapper.TransferLimitConfigMapper;
import com.macau.bank.transfer.infra.persistent.po.TransferLimitConfigPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class TransferLimitConfigRepositoryImpl implements TransferLimitConfigRepository {

    @Resource
    private TransferLimitConfigMapper transferLimitConfigMapper;

    @Resource
    private TransferLimitConfigPOConverter limitConfigPOConverter;

    @Override
    public boolean save(TransferLimitConfig limitConfig) {
        if (limitConfig == null) return false;
        TransferLimitConfigPO po = limitConfigPOConverter.toPO(limitConfig);
        int rows;
        if (Objects.isNull(po.getId())) {
            rows = transferLimitConfigMapper.insert(po);
            limitConfig.setId(po.getId());
        } else {
            rows = transferLimitConfigMapper.updateById(po);
        }
        return rows > 0;
    }

    @Override
    public TransferLimitConfig findById(Integer id) {
        TransferLimitConfigPO po = transferLimitConfigMapper.selectById(id);
        return limitConfigPOConverter.toEntity(po);
    }

    @Override
    public List<TransferLimitConfig> findAll() {
        return transferLimitConfigMapper.selectList(null).stream()
                .map(limitConfigPOConverter::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public TransferLimitConfig findMatch(String userLevel, com.macau.bank.common.core.enums.TransferType transferType, String currency) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TransferLimitConfigPO> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        wrapper.eq(TransferLimitConfigPO::getUserLevel, userLevel);
        wrapper.eq(TransferLimitConfigPO::getTransferType, transferType);
        wrapper.eq(TransferLimitConfigPO::getCurrency, currency);
        wrapper.eq(TransferLimitConfigPO::getStatus, ConfigStatusEnum.ENABLED);
        
        TransferLimitConfigPO po = transferLimitConfigMapper.selectOne(wrapper);
        return limitConfigPOConverter.toEntity(po);
    }
}
