package com.macau.bank.transfer.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.transfer.domain.entity.BankClearingCode;
import com.macau.bank.transfer.domain.repository.BankClearingCodeRepository;
import com.macau.bank.transfer.infra.persistent.converter.BankClearingCodePOConverter;
import com.macau.bank.transfer.infra.mapper.BankClearingCodeMapper;
import com.macau.bank.transfer.infra.persistent.po.BankClearingCodePO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class BankClearingCodeRepositoryImpl implements BankClearingCodeRepository {

    @Resource
    private BankClearingCodeMapper bankClearingCodeMapper;

    @Resource
    private BankClearingCodePOConverter clearingCodePOConverter;

    @Override
    public boolean save(BankClearingCode clearingCode) {
        if (clearingCode == null) return false;
        BankClearingCodePO po = clearingCodePOConverter.toPO(clearingCode);
        int rows;
        if (Objects.isNull(po.getId())) {
            rows = bankClearingCodeMapper.insert(po);
            clearingCode.setId(po.getId());
        } else {
            rows = bankClearingCodeMapper.updateById(po);
        }
        return rows > 0;
    }

    @Override
    public BankClearingCode findById(Integer id) {
        BankClearingCodePO po = bankClearingCodeMapper.selectById(id);
        return clearingCodePOConverter.toEntity(po);
    }

    @Override
    public List<BankClearingCode> findByRegion(String regionCode) {
        LambdaQueryWrapper<BankClearingCodePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BankClearingCodePO::getRegionCode, regionCode);
        return bankClearingCodeMapper.selectList(wrapper).stream()
                .map(clearingCodePOConverter::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BankClearingCode> findAll() {
        return bankClearingCodeMapper.selectList(null).stream()
                .map(clearingCodePOConverter::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BankClearingCode> query(BankClearingCode condition) {
        LambdaQueryWrapper<BankClearingCodePO> wrapper = buildWrapper(condition);
        return bankClearingCodeMapper.selectList(wrapper).stream()
                .map(clearingCodePOConverter::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public BankClearingCode findOne(BankClearingCode condition) {
        LambdaQueryWrapper<BankClearingCodePO> wrapper = buildWrapper(condition);
        BankClearingCodePO po = bankClearingCodeMapper.selectOne(wrapper);
        return clearingCodePOConverter.toEntity(po);
    }

    private LambdaQueryWrapper<BankClearingCodePO> buildWrapper(BankClearingCode condition) {
        LambdaQueryWrapper<BankClearingCodePO> wrapper = new LambdaQueryWrapper<>();
        if (condition != null) {
            wrapper.eq(condition.getRegionCode() != null, BankClearingCodePO::getRegionCode, condition.getRegionCode());
            wrapper.eq(condition.getBankCode() != null, BankClearingCodePO::getBankCode, condition.getBankCode());
            wrapper.like(condition.getBankNameCn() != null, BankClearingCodePO::getBankNameCn, condition.getBankNameCn());
            wrapper.eq(condition.getIsHot() != null, BankClearingCodePO::getIsHot, condition.getIsHot());
            wrapper.eq(condition.getStatus() != null, BankClearingCodePO::getStatus, condition.getStatus());
        }
        wrapper.orderByAsc(BankClearingCodePO::getSortOrder);
        return wrapper;
    }
}
