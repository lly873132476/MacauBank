package com.macau.bank.transfer.domain.repository;

import com.macau.bank.transfer.domain.entity.AuditLog;

/**
 * 审计日志仓储接口
 * <p>
 * 定义审计日志的持久化契约，由 Infrastructure 层实现。
 * 遵循 DDD 架构：Domain 层定义接口，Infra 层提供实现。
 */
public interface AuditLogRepository {

    /**
     * 保存审计日志
     *
     * @param auditLog 审计日志领域实体
     */
    void save(AuditLog auditLog);
}
