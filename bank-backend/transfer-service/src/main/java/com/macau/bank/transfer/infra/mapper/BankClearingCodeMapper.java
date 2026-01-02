package com.macau.bank.transfer.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macau.bank.transfer.infra.persistent.po.BankClearingCodePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BankClearingCodeMapper extends BaseMapper<BankClearingCodePO> {
}