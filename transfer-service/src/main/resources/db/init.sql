-- =============================================
-- 澳门银行-支付-微服务系统数据库脚本
-- 数据库: macau_bank_trans
-- 字符集: UTF-8
-- 时区: Asia/Macau (UTC+8)
-- =============================================

CREATE DATABASE IF NOT EXISTS macau_bank_trans DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE macau_bank_trans;

-- =============================================
-- 1. 转账订单表
-- =============================================
CREATE TABLE `transfer_order` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
      `txn_id` varchar(64) NOT NULL COMMENT '本行交易流水号（唯一，银行级）',
      `external_txn_id` varchar(128) DEFAULT NULL COMMENT '外部通道/清算返回流水号',
      `idempotent_key` varchar(100) NOT NULL COMMENT '幂等键',
      `user_no` varchar(32) NOT NULL COMMENT '本行用户编号',
      `payer_account_id` bigint(20) NOT NULL COMMENT '付款账户ID（本行账户）',
      `payer_account_no` varchar(50) NOT NULL COMMENT '付款账户号',
      `payer_account_name` varchar(100) NOT NULL COMMENT '付款账户名',
      `payer_currency` varchar(10) NOT NULL COMMENT '付款账户币种',
      `payee_account_no` varchar(50) NOT NULL COMMENT '收款账户号',
      `payee_account_name` varchar(100) DEFAULT NULL COMMENT '收款人姓名',
      `payee_bank_code` varchar(20) DEFAULT NULL COMMENT '收款银行代码',
      `payee_swift_code` varchar(20) DEFAULT NULL COMMENT 'SWIFT代码',
      `payee_fps_id` varchar(40) DEFAULT NULL COMMENT 'FPS ID（可选）',
      `amount` decimal(18,2) NOT NULL COMMENT '金额（交易币种）',
      `currency_code` varchar(10) NOT NULL COMMENT '交易币种',
      `fee` decimal(18,2) DEFAULT '0.00' COMMENT '手续费',
      `fee_type` varchar(20) DEFAULT 'SHA' COMMENT 'SHA/OUR/BEN',
      `transfer_type` varchar(30) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/CROSS_BORDER/FPS/SWIFT',
      `transfer_channel` varchar(30) NOT NULL COMMENT 'INTERNAL/SWIFT/FPS/CIPS/LOCAL_CLEARING',
      `purpose_code` varchar(20) DEFAULT NULL COMMENT '跨境用途代码',
      `reference_no` varchar(100) DEFAULT NULL COMMENT '外部参考号，如 MT103 ID',
      `status` varchar(20) NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING/SUCCESS/FAILED',
      `remark` varchar(255) DEFAULT NULL COMMENT '备注',
      `fail_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',
      `aml_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'AML状态：PENDING/PASSED/REJECTED/MANUAL_REVIEW/REPORTED',
      `aml_detail` json DEFAULT NULL COMMENT 'AML详情（命中名单、供应商返回内容）',
      `risk_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '风控状态：PENDING/PASSED/REJECTED/MANUAL_REVIEW',
      `risk_detail` json DEFAULT NULL COMMENT '风控详情（模型评分、命中规则、行为标签）',
      `extend_info` json DEFAULT NULL COMMENT '扩展字段（JSON），存储设备信息、特殊通道字段',
      `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
      `version` int(11) DEFAULT '0' COMMENT '乐观锁',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      PRIMARY KEY (`id`),
      UNIQUE KEY `idempotent_key` (`idempotent_key`),
      UNIQUE KEY `uq_txn_id` (`txn_id`),
      UNIQUE KEY `uq_idempotent` (`idempotent_key`),
      KEY `idx_user_no` (`user_no`),
      KEY `idx_payer_account_id` (`payer_account_id`),
      KEY `idx_payee_account_no` (`payee_account_no`),
      KEY `idx_payee_bank` (`payee_bank_code`,`payee_swift_code`),
      KEY `idx_status` (`status`),
      KEY `idx_transfer_channel` (`transfer_channel`),
      KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转账订单表';


-- =============================================
-- 2. 收款人表（常用/历史）
-- =============================================
CREATE TABLE `transfer_payee_book` (
       `id` bigint(20) NOT NULL AUTO_INCREMENT,
       `user_no` varchar(32) NOT NULL COMMENT '所属用户编号',
       `payee_type` tinyint(4) DEFAULT '0' COMMENT '类型: 0-历史记录(自动保存) 1-常用联系人(手动收藏)',
       `is_top` tinyint(4) DEFAULT '0' COMMENT '是否置顶: 1-是 0-否 (仅常用联系人可用)',
       `alias_name` varchar(50) NOT NULL COMMENT '别名 (如: 房东, 儿子)',
       `payee_name` varchar(100) NOT NULL COMMENT '收款人户名 (严格匹配)',
       `avatar` varchar(255) DEFAULT NULL COMMENT '收款人头像',
       `account_no` varchar(50) NOT NULL COMMENT '收款账号/IBAN',
       `currency_code` varchar(3) DEFAULT NULL COMMENT '默认币种',
       `transfer_type` varchar(20) NOT NULL DEFAULT 'INTERNAL' COMMENT 'INTERNAL/LOCAL/CROSS_BORDER',
       `bank_name` varchar(100) DEFAULT NULL COMMENT '银行名称',
       `bank_code` varchar(20) DEFAULT NULL COMMENT '银行简码',
       `region_code` varchar(5) DEFAULT 'MO',
       `swift_code` varchar(11) DEFAULT NULL,
       `clearing_code` varchar(30) DEFAULT NULL,
       `bank_address` varchar(200) DEFAULT NULL,
       `is_internal` tinyint(4) DEFAULT '0' COMMENT '是否本行: 1-是 0-否',
       `last_trans_time` datetime DEFAULT NULL COMMENT '最近转账时间',
       `total_trans_count` int(11) DEFAULT '0' COMMENT '累计次数',
       `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       PRIMARY KEY (`id`),
       KEY `idx_user_type` (`user_no`,`payee_type`,`is_top`),
       KEY `idx_user_time` (`user_no`,`last_trans_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款人表(含历史与常用)';



USE `macau_bank_trans`;

DELETE
FROM `bank_clearing_code`;

-- ==========================================
-- 1. 澳门本地银行 (支持 FPS 和 SWIFT)
-- ==========================================
INSERT INTO `bank_clearing_code`
(`region_code`, `bank_code`, `bank_name_cn`, `bank_name_en`, `logo_url`, `clearing_code`, `swift_code`, `is_hot`,
 `support_channel`, `sort_order`)
VALUES ('MO', 'BOCM', '中国银行澳门分行', 'Bank of China Macau Branch', '/static/logo/boc.png', '106', 'BKCHMOMO', 1,
        'FPS,SWIFT', 1),
       ('MO', 'BNU', '大西洋银行', 'Banco Nacional Ultramarino', '/static/logo/bnu.png', '107', 'BNUAMOMO', 1,
        'FPS,SWIFT', 2),
       ('MO', 'ICBC', '工银澳门', 'ICBC (Macau)', '/static/logo/icbc.png', '118', 'ICBKMOMO', 1, 'FPS,SWIFT', 3),
       ('MO', 'TF', '大丰银行', 'Tai Fung Bank', '/static/logo/taifung.png', '109', 'TFUNMOMO', 0, 'FPS,SWIFT', 4),
       ('MO', 'OCBC', '澳门华侨永亨银行', 'OCBC Wing Hang Bank', '/static/logo/ocbc.png', '115', 'WHBCMOMO', 0,
        'FPS,SWIFT', 5),
       ('MO', 'LUSO', '澳门国际银行', 'Luso International Banking', '/static/logo/luso.png', '111', 'LIBOMOMO', 0,
        'FPS,SWIFT', 6);

-- ==========================================
-- 2. 香港银行 (主要走 SWIFT)
-- ==========================================
INSERT INTO `bank_clearing_code`
(`region_code`, `bank_code`, `bank_name_cn`, `bank_name_en`, `logo_url`, `clearing_code`, `swift_code`, `is_hot`,
 `support_channel`, `sort_order`)
VALUES ('HK', 'HSBC', '汇丰银行(香港)', 'HSBC Hong Kong', '/static/logo/hsbc.png', '004', 'HSBCHKHH', 1, 'SWIFT', 10),
       ('HK', 'BOCHK', '中国银行(香港)', 'Bank of China (Hong Kong)', '/static/logo/bochk.png', '012', 'BKCHHKHH', 1,
        'SWIFT', 11),
       ('HK', 'SC', '渣打银行(香港)', 'Standard Chartered Hong Kong', '/static/logo/sc.png', '003', 'SCBLHKHH', 1,
        'SWIFT', 12);

-- ==========================================
-- 3. 内地银行 (主要走 SWIFT/跨境人民币)
-- ==========================================
INSERT INTO `bank_clearing_code`
(`region_code`, `bank_code`, `bank_name_cn`, `bank_name_en`, `logo_url`, `clearing_code`, `swift_code`, `is_hot`,
 `support_channel`, `sort_order`)
VALUES ('CN', 'ICBC_CN', '中国工商银行', 'ICBC China', '/static/logo/icbc.png', NULL, 'ICBKCNBJ', 1, 'SWIFT', 20),
       ('CN', 'BOC_CN', '中国银行', 'Bank of China', '/static/logo/boc.png', NULL, 'BKCHCNBJ', 1, 'SWIFT', 21);


DELETE
FROM `transfer_limit_config`;

-- ==========================================
-- 1. 普通客户 (NORMAL) - 风控较严
-- ==========================================
INSERT INTO `transfer_limit_config`
(`user_level`, `transfer_type`, `currency`, `single_limit`, `daily_limit`, `monthly_limit`)
VALUES ('NORMAL', 'INTERNAL', 'MOP', 100000.00, 200000.00, 1000000.00), -- 行内转账
       ('NORMAL', 'FPS', 'MOP', 5000.00, 10000.00, 50000.00),           -- FPS (小额快付)
       ('NORMAL', 'SWIFT', 'MOP', 50000.00, 100000.00, 500000.00);
-- 跨境汇款

-- ==========================================
-- 2. 黄金客户 (GOLD) - 额度翻倍
-- ==========================================
INSERT INTO `transfer_limit_config`
(`user_level`, `transfer_type`, `currency`, `single_limit`, `daily_limit`, `monthly_limit`)
VALUES ('GOLD', 'INTERNAL', 'MOP', 500000.00, 1000000.00, 5000000.00),
       ('GOLD', 'FPS', 'MOP', 20000.00, 50000.00, 200000.00),
       ('GOLD', 'SWIFT', 'MOP', 200000.00, 500000.00, 2000000.00);

-- ==========================================
-- 3. 钻石/VIP客户 (DIAMOND) - 高额通道
-- ==========================================
INSERT INTO `transfer_limit_config`
(`user_level`, `transfer_type`, `currency`, `single_limit`, `daily_limit`, `monthly_limit`)
VALUES ('DIAMOND', 'INTERNAL', 'MOP', 9999999.00, 9999999.00, 99999999.00),
       ('DIAMOND', 'FPS', 'MOP', 100000.00, 200000.00, 1000000.00),
       ('DIAMOND', 'SWIFT', 'MOP', 5000000.00, 10000000.00, 50000000.00);

DELETE FROM `transfer_fee_config`;

-- ==========================================
-- 1. 行内转账 (INTERNAL) - 全员免费
-- ==========================================
INSERT INTO `transfer_fee_config`
(`transfer_channel`, `currency_code`, `user_level`, `fee_type`, `deduct_type`, `fee_currency`, `calc_mode`,
 `fixed_amount`, `rate`, `min_fee`, `max_fee`, `description`)
VALUES ('INTERNAL', 'MOP', 'ALL', 'ALL', 1, 'MOP', 1, 0.00, 0.0000, 0.00, 0.00, '行内转账免费');


-- ==========================================
-- 2. FPS 转账 (MOP) - 体现等级优惠
-- ==========================================
-- NORMAL: 收 5.00 MOP
INSERT INTO `transfer_fee_config`
(`transfer_channel`, `currency_code`, `user_level`, `fee_type`, `deduct_type`, `fee_currency`, `calc_mode`,
 `fixed_amount`, `rate`, `min_fee`, `max_fee`, `description`)
VALUES ('FPS', 'MOP', 'NORMAL', 'SHA', 1, 'MOP', 1, 5.00, 0.0000, 5.00, 5.00, '普通用户FPS收费');

-- GOLD: 收 2.50 MOP (5折)
INSERT INTO `transfer_fee_config`
(`transfer_channel`, `currency_code`, `user_level`, `fee_type`, `deduct_type`, `fee_currency`, `calc_mode`,
 `fixed_amount`, `rate`, `min_fee`, `max_fee`, `description`)
VALUES ('FPS', 'MOP', 'GOLD', 'SHA', 1, 'MOP', 1, 2.50, 0.0000, 2.50, 2.50, '黄金用户FPS五折');

-- DIAMOND: 0.00 MOP (免费)
INSERT INTO `transfer_fee_config`
(`transfer_channel`, `currency_code`, `user_level`, `fee_type`, `deduct_type`, `fee_currency`, `calc_mode`,
 `fixed_amount`, `rate`, `min_fee`, `max_fee`, `description`)
VALUES ('FPS', 'MOP', 'DIAMOND', 'SHA', 1, 'MOP', 1, 0.00, 0.0000, 0.00, 0.00, '钻石用户FPS免费');


-- ==========================================
-- 3. SWIFT 跨境汇款 (核心算费逻辑)
-- 场景: 汇款 MOP/HKD/USD
-- ==========================================

-- 3.1 [NORMAL 用户] + [SHA 模式] (共同承担)
-- 规则: 100 MOP 电报费 + 0.1% 手续费 (最低50, 最高500)
-- 扣费: 外扣 (deduct_type=1)
INSERT INTO `transfer_fee_config`
(`transfer_channel`, `currency_code`, `user_level`, `fee_type`, `deduct_type`, `fee_currency`, `calc_mode`,
 `fixed_amount`, `rate`, `min_fee`, `max_fee`, `description`)
VALUES ('SWIFT', 'MOP', 'NORMAL', 'SHA', 1, 'MOP', 3, 100.00, 0.0010, 150.00, 600.00, 'SWIFT标准SHA-外扣');

-- 3.2 [NORMAL 用户] + [SHA 模式] + [内扣] (全额转出场景)
-- 规则: 同上，但 deduct_type=2
INSERT INTO `transfer_fee_config`
(`transfer_channel`, `currency_code`, `user_level`, `fee_type`, `deduct_type`, `fee_currency`, `calc_mode`,
 `fixed_amount`, `rate`, `min_fee`, `max_fee`, `description`)
VALUES ('SWIFT', 'MOP', 'NORMAL', 'SHA', 2, 'MOP', 3, 100.00, 0.0010, 150.00, 600.00, 'SWIFT标准SHA-内扣');

-- 3.3 [NORMAL 用户] + [OUR 模式] (我全包)
-- 规则: 100 MOP 电报费 + 200 MOP 代理行费(垫付) + 0.1% 手续费
-- 总固定费: 300 MOP (非常贵!)
INSERT INTO `transfer_fee_config`
(`transfer_channel`, `currency_code`, `user_level`, `fee_type`, `deduct_type`, `fee_currency`, `calc_mode`,
 `fixed_amount`, `rate`, `min_fee`, `max_fee`, `description`)
VALUES ('SWIFT', 'MOP', 'NORMAL', 'OUR', 1, 'MOP', 3, 300.00, 0.0010, 350.00, 800.00, 'SWIFT标准OUR-全包');

-- 3.4 [DIAMOND 用户] + [SHA 模式] (VIP 优惠)
-- 规则: 免电报费，只收 0.05% 手续费 (最低 20)
INSERT INTO `transfer_fee_config`
(`transfer_channel`, `currency_code`, `user_level`, `fee_type`, `deduct_type`, `fee_currency`, `calc_mode`,
 `fixed_amount`, `rate`, `min_fee`, `max_fee`, `description`)
VALUES ('SWIFT', 'MOP', 'DIAMOND', 'SHA', 1, 'MOP', 3, 0.00, 0.0005, 20.00, 200.00, '钻石SWIFT-SHA优惠');

-- 3.5 [BEN 模式] (收款人付)
-- 规则: 汇款人费用为 0 (费用在中间行扣)
INSERT INTO `transfer_fee_config`
(`transfer_channel`, `currency_code`, `user_level`, `fee_type`, `deduct_type`, `fee_currency`, `calc_mode`,
 `fixed_amount`, `rate`, `min_fee`, `max_fee`, `description`)
VALUES ('SWIFT', 'MOP', 'ALL', 'BEN', 2, 'MOP', 1, 0.00, 0.0000, 0.00, 0.00, 'SWIFT-BEN模式');




