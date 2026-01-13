package com.macau.bank.transfer.domain.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 审计日志领域实体
 * <p>
 * 领域层的纯洁实体，不依赖任何基础设施层注解
 * 用于记录系统中所有关键操作的审计轨迹
 */
@Getter
@Setter
@ToString
public class AuditLog {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 操作用户ID
     */
    private String userId;

    /**
     * 操作类型（如：TRANSFER_SUBMIT）
     */
    private String action;

    /**
     * 目标类型（如：TRANSFER_ORDER）
     */
    private String targetType;

    /**
     * 目标ID
     */
    private String targetId;

    /**
     * 操作前数据快照（JSON）
     */
    private String beforeData;

    /**
     * 操作后数据/请求参数（JSON）
     */
    private String afterData;

    /**
     * 操作结果（SUCCESS/FAILURE）
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 请求IP地址
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 操作耗时（毫秒）
     */
    private Long duration;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
