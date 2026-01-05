-- ============================================================
-- 审计日志表
-- 用于记录系统中所有关键操作的审计轨迹
-- ============================================================

CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trace_id` VARCHAR(64) NOT NULL COMMENT '链路追踪ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '操作用户ID',
    `action` VARCHAR(64) NOT NULL COMMENT '操作类型',
    `target_type` VARCHAR(32) DEFAULT NULL COMMENT '目标类型',
    `target_id` VARCHAR(64) DEFAULT NULL COMMENT '目标ID',
    `before_data` TEXT DEFAULT NULL COMMENT '操作前数据（JSON）',
    `after_data` TEXT DEFAULT NULL COMMENT '操作后数据/请求参数（JSON）',
    `result` VARCHAR(16) DEFAULT NULL COMMENT '操作结果：SUCCESS/FAILURE',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `client_ip` VARCHAR(64) DEFAULT NULL COMMENT '客户端IP',
    `user_agent` VARCHAR(256) DEFAULT NULL COMMENT '用户代理',
    `duration` BIGINT DEFAULT NULL COMMENT '操作耗时（毫秒）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_trace_id` (`trace_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';
