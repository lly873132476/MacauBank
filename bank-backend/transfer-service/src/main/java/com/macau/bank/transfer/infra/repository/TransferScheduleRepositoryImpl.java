package com.macau.bank.transfer.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.transfer.domain.entity.TransferSchedule;
import com.macau.bank.transfer.domain.repository.TransferScheduleRepository;
import com.macau.bank.transfer.infra.persistent.converter.TransferSchedulePOConverter;
import com.macau.bank.transfer.infra.mapper.TransferScheduleMapper;
import com.macau.bank.transfer.infra.persistent.po.TransferSchedulePO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class TransferScheduleRepositoryImpl implements TransferScheduleRepository {

    @Resource
    private TransferScheduleMapper transferScheduleMapper;

    @Resource
    private TransferSchedulePOConverter schedulePOConverter;

    @Override
    public boolean save(TransferSchedule schedule) {
        if (schedule == null) {
            return false;
        }
        TransferSchedulePO po = schedulePOConverter.toPO(schedule);
        int rows;
        if (Objects.isNull(po.getId())) {
            rows = transferScheduleMapper.insert(po);
            schedule.setId(po.getId());
        } else {
            rows = transferScheduleMapper.updateById(po);
        }
        return rows > 0;
    }

    @Override
    public TransferSchedule findById(Long id) {
        TransferSchedulePO po = transferScheduleMapper.selectById(id);
        return schedulePOConverter.toEntity(po);
    }

    @Override
    public List<TransferSchedule> findByUserNo(String userNo) {
        LambdaQueryWrapper<TransferSchedulePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransferSchedulePO::getUserNo, userNo);
        return transferScheduleMapper.selectList(wrapper).stream()
                .map(t -> schedulePOConverter.toEntity(t))
                .collect(Collectors.toList());
    }
}
