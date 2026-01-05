package com.macau.bank.transfer.infra.persistent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 * <p>
 * 记录系统中所有关键操作的审计轨迹，用于：
 * 1. 事后问题追溯
 * 2. 合规审计
 * 3. 安全事件分析
 */
@Getter
@Setter
@ToString
@TableName("audit_log")
public class AuditLogDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 链路追踪ID
     * <p>
     * 用于关联同一请求的所有日志，便于问题排查
     */
    private String traceId;

    /**
     * 操作用户ID
     */
    private String userId;

    /**
     * 操作类型
     * <p>
     * 示例：TRANSFER_SUBMIT, ACCOUNT_FREEZE, PAYEE_ADD
     */
    private String action;

    /**
     * 目标类型
     * <p>
     * 示例：TRANSFER_ORDER, ACCOUNT, PAYEE
     */
    private String targetType;

    /**
     * 目标ID
     * <p>
     * 被操作对象的唯一标识
     */
    private String targetId;

    /**
     * 操作前数据快照（JSON）
     * <p>
     * 用于对比操作前后的变化
     */
    private String beforeData;

    /**
     * 操作后数据/请求参数（JSON）
     */
    private String afterData;

    /**
     * 操作结果
     * <p>
     * SUCCESS / FAILURE
     */
    private String result;

    /**
     * 错误信息（如果操作失败）
     */
    private String errorMessage;

    /**
     * 请求IP地址
     */
    private String clientIp;

    /**
     * 用户代理（浏览器/客户端信息）
     */
    private String userAgent;

    /**
     * 操作耗时（毫秒）
     */
    private Long duration;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
