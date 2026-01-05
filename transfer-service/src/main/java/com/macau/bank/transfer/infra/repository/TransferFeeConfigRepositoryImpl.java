package com.macau.bank.transfer.infra.repository;

import com.macau.bank.transfer.common.enums.ConfigStatusEnum;
import com.macau.bank.transfer.domain.entity.TransferFeeConfig;
import com.macau.bank.transfer.domain.repository.TransferFeeConfigRepository;
import com.macau.bank.transfer.infra.persistent.converter.TransferFeeConfigPOConverter;
import com.macau.bank.transfer.infra.mapper.TransferFeeConfigMapper;
import com.macau.bank.transfer.infra.persistent.po.TransferFeeConfigPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class TransferFeeConfigRepositoryImpl implements TransferFeeConfigRepository {

    @Resource
    private TransferFeeConfigMapper transferFeeConfigMapper;

    @Resource
    private TransferFeeConfigPOConverter feeConfigPOConverter;

    @Override
    public boolean save(TransferFeeConfig feeConfig) {
        if (feeConfig == null) return false;
        TransferFeeConfigPO po = feeConfigPOConverter.toPO(feeConfig);
        int rows;
        if (Objects.isNull(po.getId())) {
            rows = transferFeeConfigMapper.insert(po);
            feeConfig.setId(po.getId());
        } else {
            rows = transferFeeConfigMapper.updateById(po);
        }
        return rows > 0;
    }

    @Override
    public TransferFeeConfig findById(Integer id) {
        TransferFeeConfigPO po = transferFeeConfigMapper.selectById(id);
        return feeConfigPOConverter.toEntity(po);
    }

    @Override
    public List<TransferFeeConfig> findAll() {
        return transferFeeConfigMapper.selectList(null).stream()
                .map(feeConfigPOConverter::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public TransferFeeConfig findMatch(String channel, String currency, String userLevel) {
        // 这里的查询逻辑需要谨慎，可能涉及优先级或模糊匹配
        // 假设是精确匹配
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TransferFeeConfigPO> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        wrapper.eq(TransferFeeConfigPO::getTransferChannel, channel);
        wrapper.eq(TransferFeeConfigPO::getCurrencyCode, currency);
        wrapper.eq(TransferFeeConfigPO::getUserLevel, userLevel);
        wrapper.eq(TransferFeeConfigPO::getStatus, ConfigStatusEnum.ENABLED);
        
        TransferFeeConfigPO po = transferFeeConfigMapper.selectOne(wrapper);
        return feeConfigPOConverter.toEntity(po);
    }
}
