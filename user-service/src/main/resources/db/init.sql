-- =============================================
-- 澳门银行-用户-微服务系统数据库脚本
-- 数据库: macau_bank_user
-- 字符集: UTF-8
-- 时区: Asia/Macau (UTC+8)
-- =============================================

CREATE DATABASE IF NOT EXISTS macau_bank_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE macau_bank_user;

-- =============================================
-- 1. 用户详细档案表
-- =============================================
-- user_info: table
CREATE TABLE `user_info` (
     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
     `user_no` varchar(32) NOT NULL COMMENT '关联 user_auth.user_no',

    -- 姓名信息
     `real_name_cn` varchar(100) DEFAULT NULL COMMENT '中文姓名 (如: 陈大文)',
     `real_name_en` varchar(100) DEFAULT NULL COMMENT '英文/葡文姓名 (如: CHAN TAI MAN)',

    -- 证件信息 (核心敏感数据)
     `id_card_type` tinyint DEFAULT '1' COMMENT '证件类型: 1-澳门身份证(BIR) 2-回乡证 3-护照 4-蓝卡',
     `id_card_no` varchar(255) DEFAULT NULL COMMENT '证件号码 (密文存储, 长度预留充足)',
     `id_card_expiry` date DEFAULT NULL COMMENT '证件有效期',
     `id_card_issue_country` varchar(50) DEFAULT 'Macau' COMMENT '发证国家/地区',
     `id_card_issue_org` varchar(100) DEFAULT 'DSI' COMMENT '发证机关',
     `id_card_img_front` varchar(255) DEFAULT NULL COMMENT '证件正面图片路径',
     `id_card_img_back` varchar(255) DEFAULT NULL COMMENT '证件背面图片路径',

    -- KYC 核心控制 (等级 vs 状态)
     `kyc_level` tinyint DEFAULT '0' COMMENT '认证等级: 0-匿名 1-初级(L1) 2-高级(L2)',
     `kyc_status` tinyint DEFAULT '0' COMMENT '认证状态: 0-未认证 1-审核中 2-审核通过 3-审核驳回',
     `user_level` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '客户等级: NORMAL-普通, GOLD-黄金, DIAMOND-钻石',

    -- 个人资料
     `gender` tinyint DEFAULT '0' COMMENT '性别 0:未知 1:男 2:女',
     `birthday` date DEFAULT NULL COMMENT '出生日期',
     `nationality` varchar(50) DEFAULT 'Macau' COMMENT '国籍/地区',
     `occupation` varchar(100) DEFAULT NULL COMMENT '职业',
     `employment_status` tinyint DEFAULT '1' COMMENT '就业状态: 1-受雇 2-自雇 3-待业 4-退休 5-学生',
     `tax_id` varchar(50) DEFAULT NULL COMMENT '税务编号 (TIN)',

    -- 联系地址
     `address_region` varchar(50) DEFAULT NULL COMMENT '地区 (如: 澳门半岛/氹仔/路环)',
     `address_detail` varchar(255) DEFAULT NULL COMMENT '详细地址',

    -- 系统字段
     `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_user_no` (`user_no`),
     KEY `idx_real_name_cn` (`real_name_cn`),
     KEY `idx_user_level` (`user_level`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户详细档案表';










