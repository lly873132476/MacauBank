package com.macau.bank.transfer.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.query.TransferOrderQuery;
import com.macau.bank.transfer.domain.repository.TransferOrderRepository;
import com.macau.bank.transfer.infra.mapper.TransferOrderMapper;
import com.macau.bank.transfer.infra.persistent.converter.TransferOrderPOConverter;
import com.macau.bank.transfer.infra.persistent.po.TransferOrderPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class TransferOrderRepositoryImpl implements TransferOrderRepository {

    @Resource
    private TransferOrderMapper transferOrderMapper;

    @Resource
    private TransferOrderPOConverter orderPOConverter;

    @Override
    public boolean save(TransferOrder transferOrder) {
        if (transferOrder == null) {
            return false;
        }
        TransferOrderPO po = orderPOConverter.toPO(transferOrder);

        int rows;
        if (Objects.isNull(po.getId())) {
            rows = transferOrderMapper.insert(po);
            // 回填ID到领域对象
            transferOrder.setId(po.getId());
        } else {
            rows = transferOrderMapper.updateById(po);
        }
        return rows > 0;
    }

    @Override
    public TransferOrder findById(Long id) {
        if (id == null) {
            return null;
        }
        TransferOrderPO po = transferOrderMapper.selectById(id);
        return orderPOConverter.toEntity(po);
    }

    @Override
    public TransferOrder findByTxnId(String txnId) {
        LambdaQueryWrapper<TransferOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransferOrderPO::getTxnId, txnId);
        TransferOrderPO po = transferOrderMapper.selectOne(wrapper);
        return orderPOConverter.toEntity(po);
    }

    @Override
    public java.util.List<TransferOrder> query(TransferOrderQuery condition) {
        LambdaQueryWrapper<TransferOrderPO> wrapper = new LambdaQueryWrapper<>();
        if (condition != null) {
            String userNo = condition.getPayerUserNo();
            String payeeAcc = condition.getPayeeAccountNo();

            wrapper.eq(userNo != null, TransferOrderPO::getUserNo, userNo);
            wrapper.eq(payeeAcc != null && !payeeAcc.isEmpty(),
                    TransferOrderPO::getPayeeAccountNo, payeeAcc);
            wrapper.eq(condition.getStatus() != null, TransferOrderPO::getStatus, condition.getStatus());
        }
        wrapper.orderByDesc(TransferOrderPO::getCreateTime);

        java.util.List<TransferOrderPO> poList = transferOrderMapper.selectList(wrapper);
        return poList.stream()
                .map(orderPOConverter::toEntity)
                .collect(java.util.stream.Collectors.toList());
    }

}
