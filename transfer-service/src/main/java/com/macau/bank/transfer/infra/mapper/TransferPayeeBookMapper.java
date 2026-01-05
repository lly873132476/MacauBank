package com.macau.bank.transfer.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macau.bank.transfer.infra.persistent.po.TransferPayeeBookPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransferPayeeBookMapper extends BaseMapper<TransferPayeeBookPO> {
}