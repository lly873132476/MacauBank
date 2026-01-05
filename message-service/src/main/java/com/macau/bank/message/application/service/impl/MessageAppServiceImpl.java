package com.macau.bank.message.application.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.message.application.assembler.MessageDomainAssembler;
import com.macau.bank.message.application.result.MessageResult;
import com.macau.bank.message.application.service.MessageAppService;
import com.macau.bank.message.domain.service.MessageDomainService;
import com.macau.bank.message.domain.entity.StationMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息应用服务实现 - 重构优化版
 */
@Slf4j
@Service
public class MessageAppServiceImpl implements MessageAppService {

    @Resource
    private MessageDomainService messageDomainService;

    @Resource
    private MessageDomainAssembler messageDomainAssembler;

    @Override
    public IPage<MessageResult> getMessagePage(String userNo, int page, int pageSize) {
        log.info("应用服务 - 分页查询消息列表: userNo={}, page={}, size={}", userNo, page, pageSize);

        // 调用领域服务进行带分页的查询
        IPage<StationMessage> messagePage = messageDomainService.getMessagePage(userNo, page, pageSize);

        // 转换模型
        return messagePage.convert(messageDomainAssembler::toResult);
    }

    @Override
    public void markAsRead(String userNo, String messageId) {
        log.info("应用服务 - 标记已读: userNo={}, messageId={}", userNo, messageId);
        messageDomainService.markAsRead(userNo, messageId);
    }

    @Override
    public Integer getUnreadCount(String userNo) {
        log.debug("应用服务 - 获取未读数量: userNo={}", userNo);
        return messageDomainService.getUnreadCount(userNo);
    }

}
