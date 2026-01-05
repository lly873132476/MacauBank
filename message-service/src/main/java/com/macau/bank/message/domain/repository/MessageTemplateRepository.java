package com.macau.bank.message.domain.repository;

import com.macau.bank.message.domain.entity.MessageTemplate;

/**
 * 消息模板仓储接口
 * <p>
 * 归属：Domain 层
 * 职责：定义消息模板数据访问契约
 */
public interface MessageTemplateRepository {

    /**
     * 根据模板代码和状态查询模板
     */
    MessageTemplate findByCodeAndStatus(String templateCode, int status);
}
