package com.macau.bank.message.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macau.bank.message.domain.entity.MessageTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageTemplateMapper extends BaseMapper<MessageTemplate> {

}
