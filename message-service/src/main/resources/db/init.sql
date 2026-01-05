-- =============================================
-- 澳门银行-消息-微服务系统数据库脚本
-- 数据库: macau_bank_msg
-- 字符集: UTF-8
-- 时区: Asia/Macau (UTC+8)
-- =============================================

CREATE DATABASE IF NOT EXISTS macau_bank_msg DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE macau_bank_msg;

-- =============================================
-- 1. 消息模版配置表
-- =============================================
CREATE TABLE `message_template` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `template_code` varchar(50) NOT NULL COMMENT '模版代码 (如: SMS_OTP_LOGIN, MSG_TRANS_SUCCESS)',
    `channel` varchar(20) NOT NULL COMMENT '渠道: SMS, EMAIL, STATION (站内信)',
    `title_template` varchar(100) DEFAULT NULL COMMENT '标题模版 (站内信/邮件用)',
    `content_template` text NOT NULL COMMENT '内容模版 (支持占位符, 如: 您的验证码是{code})',
    `lang` varchar(10) DEFAULT 'zh_HK' COMMENT '语言: zh_HK, en_US, pt_MO',
    `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1-启用 0-停用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模版配置表';


-- =============================================
-- 2. 短信发送流水表
-- =============================================
CREATE TABLE `sms_send_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `msg_id` varchar(64) NOT NULL COMMENT '全局消息ID (Trace ID)',
    `user_no` varchar(32) DEFAULT NULL COMMENT '接收用户编号 (如果是游客发送验证码，此字段可空)',
    `mobile_prefix` varchar(10) DEFAULT '+853' COMMENT '区号',
    `mobile` varchar(20) NOT NULL COMMENT '手机号',
    `template_code` varchar(50) NOT NULL COMMENT '关联的模版Code',
    `content_params` json DEFAULT NULL COMMENT '模版参数快照 (如: {"code": "1234"})',
    `final_content` varchar(500) DEFAULT NULL COMMENT '最终发送的完整短信内容 (用于审计)',
    `provider` varchar(20) NOT NULL COMMENT '供应商: ALIYUN, TWILIO, CTM',
    `provider_msg_id` varchar(100) DEFAULT NULL COMMENT '供应商返回的流水号',
    `provider_response` text COMMENT '供应商接口返回的完整报文 (Debug神器)',
    `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-发送中 1-成功 2-失败',
    `fail_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',
    `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `msg_id` (`msg_id`),
    KEY `idx_mobile` (`mobile`),
    KEY `idx_create_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送流水表';


-- =============================================
-- 3. 用户站内信表
-- =============================================
CREATE TABLE `station_message` (
       `id` bigint(20) NOT NULL AUTO_INCREMENT,
       `msg_id` varchar(64) NOT NULL,
       `user_no` varchar(32) NOT NULL COMMENT '接收用户',
       `category` tinyint(4) DEFAULT '1' COMMENT '分类: 1-动账通知 2-安全中心 3-系统公告',
       `title` varchar(100) NOT NULL COMMENT '消息标题',
       `content` text COMMENT '消息正文 (或者HTML片段)',
       `biz_type` varchar(30) DEFAULT NULL COMMENT '业务类型: TRANS_DETAIL, KYC_RESULT',
       `biz_id` varchar(64) DEFAULT NULL COMMENT '关联业务ID (如 transfer_order.txn_id)',
       `is_read` tinyint(4) DEFAULT '0' COMMENT '是否已读: 0-未读 1-已读',
       `read_time` datetime DEFAULT NULL COMMENT '读取时间',
       `is_deleted` tinyint(4) DEFAULT '0' COMMENT '用户逻辑删除',
       `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '推送时间',
       `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
       PRIMARY KEY (`id`),
       UNIQUE KEY `msg_id` (`msg_id`),
       KEY `idx_user_read` (`user_no`,`is_read`),
       KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户站内信表'

-- ==========================================================
-- 澳门银行消息模版初始化脚本 (基于业务重构需求)
-- 包含占位符: {amount}, {currency}, {txnId}, {type}, {level}
-- ==========================================================

USE `macau_bank_msg`;

-- 清理旧数据 (可选)
-- DELETE FROM `message_template` WHERE `template_code` IN ('MSG_TRANS_SUCCESS', 'MSG_FOREX_SUCCESS', 'MSG_USER_LEVEL_UP');

INSERT INTO `message_template`
(`template_code`, `channel`, `title_template`, `content_template`, `lang`, `status`)
VALUES
-- 1. 转账成功通知 (支持站内信 + 短信双渠道)
('MSG_TRANS_SUCCESS',
 'STATION,SMS',
 '交易提醒 - {type}',
 '尊敬的客户，您的账户发生一笔{type}交易，金额为 {currency} {amount}。单号：{txnId}。若非本人操作请速联系本行。',
 'zh_HK',
 1),

-- 2. 外汇兑换成功通知
('MSG_FOREX_SUCCESS',
 'STATION',
 '外汇兑换成交通知',
 '尊敬的客户，您的外汇兑换申请已成交。获得金额：{currency} {amount}，成交单号：{txnId}。感谢您选择澳门银行。',
 'zh_HK',
 1),

-- 3. 账户等级变更通知 (安全/特权)
('MSG_USER_LEVEL_UP',
 'STATION,SMS',
 '账户等级变更提醒',
 '尊敬的客户，恭喜您的账户等级已调整为 [{level}]。即刻起您将享有更高的单笔转账限额及更优的手续费折扣。',
 'zh_HK',
 1)
ON DUPLICATE KEY UPDATE `title_template`   = VALUES(`title_template`),
                        `content_template` = VALUES(`content_template`),
                        `channel`          = VALUES(`channel`);

-- 校验数据
SELECT *
FROM `message_template`;




