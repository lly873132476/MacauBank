package com.macau.bank.account.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.account.domain.entity.AccountInfo;
import com.macau.bank.account.domain.repository.AccountInfoRepository;
import com.macau.bank.account.infra.converter.AccountInfoConverter;
import com.macau.bank.account.infra.mapper.AccountInfoMapper;
import com.macau.bank.account.infra.persistent.po.AccountInfoPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AccountInfoRepositoryImpl implements AccountInfoRepository {

    @Resource
    private AccountInfoMapper accountInfoMapper;

    @Resource
    private AccountInfoConverter accountInfoConverter;

    @Override
    public void save(AccountInfo accountInfo) {
        if (accountInfo == null) return;
        AccountInfoPO po = accountInfoConverter.toPO(accountInfo);
        if (po.getId() == null) {
            accountInfoMapper.insert(po);
            accountInfo.setId(po.getId());
        } else {
            accountInfoMapper.updateById(po);
        }
    }

    @Override
    public AccountInfo findById(Long id) {
        return accountInfoConverter.toEntity(accountInfoMapper.selectById(id));
    }

    @Override
    public AccountInfo findByAccountNo(String accountNo) {
        LambdaQueryWrapper<AccountInfoPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountInfoPO::getAccountNo, accountNo);
        return accountInfoConverter.toEntity(accountInfoMapper.selectOne(wrapper));
    }

    @Override
    public AccountInfo findByCardNumber(String cardNumber) {
        LambdaQueryWrapper<AccountInfoPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountInfoPO::getCardNumber, cardNumber);
        return accountInfoConverter.toEntity(accountInfoMapper.selectOne(wrapper));
    }

    @Override
    public List<AccountInfo> findByUserNo(String userNo) {
        LambdaQueryWrapper<AccountInfoPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountInfoPO::getUserNo, userNo);
        return accountInfoMapper.selectList(wrapper).stream()
                .map(accountInfoConverter::toEntity)
                .collect(Collectors.toList());
    }
}
