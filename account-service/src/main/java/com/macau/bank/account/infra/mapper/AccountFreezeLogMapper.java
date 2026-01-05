package com.macau.bank.account.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macau.bank.account.infra.persistent.po.AccountFreezeLogPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountFreezeLogMapper extends BaseMapper<AccountFreezeLogPO> {
}
