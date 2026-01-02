package com.macau.bank.account.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.account.domain.entity.AccountSubLedger;
import com.macau.bank.account.domain.repository.AccountSubLedgerRepository;
import com.macau.bank.account.infra.converter.AccountSubLedgerConverter;
import com.macau.bank.account.infra.mapper.AccountSubLedgerMapper;
import com.macau.bank.account.infra.persistent.po.AccountSubLedgerPO;
import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.FlowDirection;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Repository
public class AccountSubLedgerRepositoryImpl implements AccountSubLedgerRepository {

    @Resource
    private AccountSubLedgerMapper accountSubLedgerMapper;

    @Resource
    private AccountSubLedgerConverter accountSubLedgerConverter;

    @Override
    public void save(AccountSubLedger subLedger) {
        if (subLedger == null) return;
        AccountSubLedgerPO po = accountSubLedgerConverter.toPO(subLedger);
        if (po.getId() == null) {
            accountSubLedgerMapper.insert(po);
            subLedger.setId(po.getId());
        } else {
            accountSubLedgerMapper.updateById(po);
        }
    }

    @Override
    public AccountSubLedger findByRequestId(String requestId) {
        if (!StringUtils.hasText(requestId)) return null;
        LambdaQueryWrapper<AccountSubLedgerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountSubLedgerPO::getRequestId, requestId);
        return accountSubLedgerConverter.toEntity(accountSubLedgerMapper.selectOne(wrapper));
    }

    @Override
    public IPage<AccountSubLedger> page(String userNo, String accountNo, String currencyCode, LocalDate startDate, LocalDate endDate, FlowDirection direction, BizType bizType, int page, int pageSize) {
        Page<AccountSubLedgerPO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AccountSubLedgerPO> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(AccountSubLedgerPO::getUserNo, userNo);
        if (StringUtils.hasText(accountNo)) wrapper.eq(AccountSubLedgerPO::getAccountNo, accountNo);
        if (StringUtils.hasText(currencyCode)) wrapper.eq(AccountSubLedgerPO::getCurrencyCode, currencyCode);
        if (startDate != null) wrapper.ge(AccountSubLedgerPO::getAcctDate, startDate);
        if (endDate != null) wrapper.le(AccountSubLedgerPO::getAcctDate, endDate);
        if (direction != null) wrapper.eq(AccountSubLedgerPO::getCdFlag, direction);
        if (bizType != null) wrapper.eq(AccountSubLedgerPO::getBizType, bizType);
        
        wrapper.orderByDesc(AccountSubLedgerPO::getTransTime);
        
        return accountSubLedgerMapper.selectPage(pageParam, wrapper)
                .convert(accountSubLedgerConverter::toEntity);
    }
}
