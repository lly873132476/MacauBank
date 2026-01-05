package com.macau.bank.transfer.infra.repository;

import com.macau.bank.transfer.domain.repository.AuditLogRepository;
import com.macau.bank.transfer.infra.mapper.AuditLogMapper;
import com.macau.bank.transfer.infra.persistent.entity.AuditLogDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 * 审计日志仓储实现
 * <p>
 * Infrastructure 层实现，负责审计日志的持久化。
 * 使用 @Async 异步保存，避免影响主业务性能。
 */
@Slf4j
@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {

    @Resource
    private AuditLogMapper auditLogMapper;

    @Override
    @Async
    public void save(AuditLogDO auditLog) {
        try {
            auditLogMapper.insert(auditLog);
            log.debug("[Audit] 审计日志保存成功: action={}, targetId={}, duration={}ms",
                    auditLog.getAction(), auditLog.getTargetId(), auditLog.getDuration());
        } catch (Exception e) {
            // 审计日志保存失败不应影响业务，只记录错误日志
            log.error("[Audit] 审计日志保存失败: action={}, error={}",
                    auditLog.getAction(), e.getMessage(), e);
        }
    }
}
