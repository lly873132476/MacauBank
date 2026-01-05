package com.macau.bank.transfer.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.transfer.domain.entity.TransferPayeeBook;
import com.macau.bank.transfer.domain.repository.TransferPayeeBookRepository;
import com.macau.bank.transfer.infra.persistent.converter.TransferPayeeBookPOConverter;
import com.macau.bank.transfer.infra.mapper.TransferPayeeBookMapper;
import com.macau.bank.transfer.infra.persistent.po.TransferPayeeBookPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class TransferPayeeBookRepositoryImpl implements TransferPayeeBookRepository {

    @Resource
    private TransferPayeeBookMapper transferPayeeBookMapper;

    @Resource
    private TransferPayeeBookPOConverter payeeBookPOConverter;

    @Override
    public boolean save(TransferPayeeBook payeeBook) {
        if (payeeBook == null) {
            return false;
        }
        TransferPayeeBookPO po = payeeBookPOConverter.toPO(payeeBook);
        int rows;
        if (Objects.isNull(po.getId())) {
            rows = transferPayeeBookMapper.insert(po);
            payeeBook.setId(po.getId());
        } else {
            rows = transferPayeeBookMapper.updateById(po);
        }
        return rows > 0;
    }

    @Override
    public TransferPayeeBook findById(Long id) {
        TransferPayeeBookPO po = transferPayeeBookMapper.selectById(id);
        return payeeBookPOConverter.toEntity(po);
    }

    @Override
    public List<TransferPayeeBook> findByUserNo(String userNo) {
        LambdaQueryWrapper<TransferPayeeBookPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransferPayeeBookPO::getUserNo, userNo);
        return transferPayeeBookMapper.selectList(wrapper).stream()
                .map(payeeBookPOConverter::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(Long id) {
        return transferPayeeBookMapper.deleteById(id) > 0;
    }

    @Override
    public long count(TransferPayeeBook condition) {
        LambdaQueryWrapper<TransferPayeeBookPO> wrapper = buildWrapper(condition);
        return transferPayeeBookMapper.selectCount(wrapper);
    }

    @Override
    public TransferPayeeBook findOne(TransferPayeeBook condition) {
        LambdaQueryWrapper<TransferPayeeBookPO> wrapper = buildWrapper(condition);
        TransferPayeeBookPO po = transferPayeeBookMapper.selectOne(wrapper);
        return payeeBookPOConverter.toEntity(po);
    }

    @Override
    public List<TransferPayeeBook> query(TransferPayeeBook condition, int page, int size) {
        LambdaQueryWrapper<TransferPayeeBookPO> wrapper = buildWrapper(condition);
        // 简单分页实现
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<TransferPayeeBookPO> p = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        return transferPayeeBookMapper.selectPage(p, wrapper).getRecords().stream()
                .map(payeeBookPOConverter::toEntity)
                .collect(Collectors.toList());
    }

    private LambdaQueryWrapper<TransferPayeeBookPO> buildWrapper(TransferPayeeBook condition) {
        LambdaQueryWrapper<TransferPayeeBookPO> wrapper = new LambdaQueryWrapper<>();
        if (condition != null) {
            wrapper.eq(condition.getUserNo() != null, TransferPayeeBookPO::getUserNo, condition.getUserNo());
            wrapper.eq(condition.getPayeeType() != null, TransferPayeeBookPO::getPayeeType, condition.getPayeeType());
            wrapper.eq(condition.getAccountNo() != null, TransferPayeeBookPO::getAccountNo, condition.getAccountNo());
            // Add other fields as needed based on DomainService usage
        }
        return wrapper;
    }
}
