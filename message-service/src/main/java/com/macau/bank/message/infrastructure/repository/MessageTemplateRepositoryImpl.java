package com.macau.bank.message.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.message.domain.entity.MessageTemplate;
import com.macau.bank.message.domain.repository.MessageTemplateRepository;
import com.macau.bank.message.infrastructure.mapper.MessageTemplateMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * 消息模板仓储实现
 */
@Repository
public class MessageTemplateRepositoryImpl implements MessageTemplateRepository {

    @Resource
    private MessageTemplateMapper messageTemplateMapper;

    @Override
    public MessageTemplate findByCodeAndStatus(String templateCode, int status) {
        return messageTemplateMapper.selectOne(new LambdaQueryWrapper<MessageTemplate>()
                .eq(MessageTemplate::getTemplateCode, templateCode)
                .eq(MessageTemplate::getStatus, status));
    }
}
