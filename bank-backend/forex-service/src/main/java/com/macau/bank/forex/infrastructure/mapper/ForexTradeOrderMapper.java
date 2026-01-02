package com.macau.bank.forex.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macau.bank.forex.infrastructure.persistent.po.ForexTradeOrderPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ForexTradeOrderMapper extends BaseMapper<ForexTradeOrderPO> {
}
