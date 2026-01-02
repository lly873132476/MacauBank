package com.macau.bank.account.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.account.domain.entity.AccountFreezeLog;
import com.macau.bank.account.domain.repository.AccountFreezeLogRepository;
import com.macau.bank.account.infra.converter.AccountFreezeLogConverter;
import com.macau.bank.account.infra.mapper.AccountFreezeLogMapper;
import com.macau.bank.account.infra.persistent.po.AccountFreezeLogPO;
import com.macau.bank.common.core.enums.FreezeStatus;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AccountFreezeLogRepositoryImpl implements AccountFreezeLogRepository {

    @Resource
    private AccountFreezeLogMapper accountFreezeLogMapper;

    @Resource
    private AccountFreezeLogConverter accountFreezeLogConverter;

    @Override
    public void save(AccountFreezeLog freezeLog) {
        if (freezeLog == null) return;
        AccountFreezeLogPO po = accountFreezeLogConverter.toPO(freezeLog);
        if (po.getId() == null) {
            accountFreezeLogMapper.insert(po);
            freezeLog.setId(po.getId());
        } else {
            accountFreezeLogMapper.updateById(po);
        }
    }

    @Override
    public AccountFreezeLog findByFlowNo(String flowNo) {
        LambdaQueryWrapper<AccountFreezeLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountFreezeLogPO::getFlowNo, flowNo);
        return accountFreezeLogConverter.toEntity(accountFreezeLogMapper.selectOne(wrapper));
    }

    @Override
    public List<AccountFreezeLog> findDeadLogs(LocalDateTime beforeTime, int limit) {
        // 可以在这里处理分库分表逻辑，或者 MyBatis 的 Example 封装
        List<AccountFreezeLogPO> freezeLogPOS = accountFreezeLogMapper.selectList(new LambdaQueryWrapper<AccountFreezeLogPO>()
                .eq(AccountFreezeLogPO::getStatus, FreezeStatus.FROZEN)
                .lt(AccountFreezeLogPO::getCreateTime, beforeTime)
                .orderByAsc(AccountFreezeLogPO::getCreateTime)
                .last("LIMIT " + limit)
        );
        return accountFreezeLogConverter.toEntityList(freezeLogPOS);
    }
}
