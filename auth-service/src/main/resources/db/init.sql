-- =============================================
-- 澳门银行-鉴权-微服务系统数据库脚本
-- 数据库: macau_bank_auth
-- 字符集: UTF-8
-- 时区: Asia/Macau (UTC+8)
-- =============================================

CREATE DATABASE IF NOT EXISTS macau_bank_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE macau_bank_auth;

-- =============================================
-- 1. 用户认证中心表
-- =============================================
CREATE TABLE `user_auth` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
     `user_no` varchar(32) NOT NULL COMMENT '用户编号(系统内部唯一标识)',
     `user_name` varchar(50) DEFAULT NULL COMMENT '用户名(可选，部分银行APP仅允许手机号登录)',

     -- 登录凭证部分
     `mobile_prefix` varchar(10) DEFAULT '+853' COMMENT '手机区号',
     `mobile` varchar(20) NOT NULL COMMENT '手机号',

     -- 密码安全部分
     `login_password` varchar(128) NOT NULL COMMENT '登录密码（加密）',
     `transaction_password` varchar(128) DEFAULT NULL COMMENT '交易密码（加密）',

     -- 状态与审计
     `status` tinyint(4) DEFAULT '1' COMMENT '状态 0:禁用 1:正常 2:冻结',
     `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
     `last_login_ip` varchar(45) DEFAULT NULL COMMENT '最后登录IP',
     `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除 0:未删除 1:已删除',
     `version` int(11) DEFAULT '0' COMMENT '版本号（乐观锁）',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `user_no` (`user_no`),
     UNIQUE KEY `user_name` (`user_name`),
     KEY `idx_user_no` (`user_no`),
     KEY `idx_mobile` (`mobile`),
     KEY `idx_prefix_mobile` (`mobile_prefix`,`mobile`),
     KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证中心表';
