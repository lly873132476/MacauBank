package com.macau.bank.message.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.common.core.enums.YesNo;
import com.macau.bank.message.domain.entity.StationMessage;
import com.macau.bank.message.domain.repository.StationMessageRepository;
import com.macau.bank.message.infrastructure.mapper.StationMessageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内消息仓储实现
 */
@Repository
public class StationMessageRepositoryImpl implements StationMessageRepository {

    @Resource
    private StationMessageMapper stationMessageMapper;

    @Override
    public void save(StationMessage message) {
        stationMessageMapper.insert(message);
    }

    @Override
    public Long countByBizIdAndUserNo(String bizId, String userNo) {
        return stationMessageMapper.selectCount(new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getBizId, bizId)
                .eq(StationMessage::getUserNo, userNo));
    }

    @Override
    public IPage<StationMessage> pageByUserNo(String userNo, int page, int pageSize) {
        Page<StationMessage> pageParam = new Page<>(page, pageSize);
        return stationMessageMapper.selectPage(pageParam, new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getUserNo, userNo)
                .orderByDesc(StationMessage::getCreateTime));
    }

    @Override
    public int updateReadStatus(String bizId, String userNo) {
        StationMessage updateMsg = new StationMessage();
        updateMsg.setIsRead(YesNo.YES);
        updateMsg.setReadTime(LocalDateTime.now());
        updateMsg.setUpdateTime(LocalDateTime.now());

        return stationMessageMapper.update(updateMsg, new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getBizId, bizId)
                .eq(StationMessage::getUserNo, userNo));
    }

    @Override
    public Long countUnread(String userNo) {
        return stationMessageMapper.selectCount(new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getUserNo, userNo)
                .eq(StationMessage::getIsRead, YesNo.NO));
    }

    @Override
    public List<StationMessage> findByUserNo(String userNo) {
        return stationMessageMapper.selectList(new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getUserNo, userNo)
                .orderByDesc(StationMessage::getCreateTime));
    }
}
