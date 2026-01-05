package com.macau.bank.message.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.macau.bank.message.application.result.MessageResult;

/**
 * 消息应用服务接口
 */
public interface MessageAppService {
    
    /**
     * 分页查询用户消息
     */
    IPage<MessageResult> getMessagePage(String userNo, int page, int pageSize);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(String userNo, String messageId);
    
    /**
     * 获取未读消息总数
     */
    Integer getUnreadCount(String userNo);
}