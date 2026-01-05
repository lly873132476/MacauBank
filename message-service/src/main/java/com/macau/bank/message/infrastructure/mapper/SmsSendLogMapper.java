package com.macau.bank.message.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macau.bank.message.domain.entity.SmsSendLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsSendLogMapper extends BaseMapper<SmsSendLog> {

}
