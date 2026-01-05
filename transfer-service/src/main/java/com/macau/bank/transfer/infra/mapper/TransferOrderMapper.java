package com.macau.bank.transfer.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macau.bank.transfer.infra.persistent.po.TransferOrderPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransferOrderMapper extends BaseMapper<TransferOrderPO> {
}