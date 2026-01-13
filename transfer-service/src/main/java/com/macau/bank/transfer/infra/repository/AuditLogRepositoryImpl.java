package com.macau.bank.transfer.infra.repository;

import com.macau.bank.transfer.domain.entity.AuditLog;
import com.macau.bank.transfer.domain.repository.AuditLogRepository;
import com.macau.bank.transfer.infra.mapper.AuditLogMapper;
import com.macau.bank.transfer.infra.persistent.po.AuditLogPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 * 审计日志仓储实现
 * <p>
 * Infrastructure 层实现，负责审计日志的持久化。
 * 负责领域实体与持久化对象之间的转换。
 * 使用 @Async 异步保存，避免影响主业务性能。
 */
@Slf4j
@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {

    @Resource
    private AuditLogMapper auditLogMapper;

    @Override
    @Async
    public void save(AuditLog auditLog) {
        try {
            // 领域实体 -> 持久化对象转换
            AuditLogPO po = toPO(auditLog);
            auditLogMapper.insert(po);
            log.debug("[Audit] 审计日志保存成功: action={}, targetId={}, duration={}ms",
                    auditLog.getAction(), auditLog.getTargetId(), auditLog.getDuration());
        } catch (Exception e) {
            // 审计日志保存失败不应影响业务，只记录错误日志
            log.error("[Audit] 审计日志保存失败: action={}, error={}",
                    auditLog.getAction(), e.getMessage(), e);
        }
    }

    /**
     * 领域实体转持久化对象
     */
    private AuditLogPO toPO(AuditLog entity) {
        AuditLogPO po = new AuditLogPO();
        BeanUtils.copyProperties(entity, po);
        return po;
    }
}
