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
-- transfer_order: table
CREATE TABLE `transfer_order` (
          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
          `txn_id` varchar(64) NOT NULL COMMENT '本行交易流水号（唯一，银行级）',
          `external_txn_id` varchar(128) DEFAULT NULL COMMENT '外部通道/清算返回流水号',
          `idempotent_key` varchar(100) NOT NULL COMMENT '幂等键',
          `user_no` varchar(32) NOT NULL COMMENT '本行用户编号',
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
          `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
          `version` int DEFAULT '0' COMMENT '乐观锁',
          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
          PRIMARY KEY (`id`),
          UNIQUE KEY `idempotent_key` (`idempotent_key`),
          UNIQUE KEY `uq_txn_id` (`txn_id`),
          UNIQUE KEY `uq_idempotent` (`idempotent_key`),
          KEY `idx_user_no` (`user_no`),
          KEY `idx_payee_account_no` (`payee_account_no`),
          KEY `idx_payee_bank` (`payee_bank_code`,`payee_swift_code`),
          KEY `idx_transfer_channel` (`transfer_channel`),
          KEY `idx_create_time` (`create_time`),
          KEY `idx_status_update_time` (`status`,`update_time`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转账订单表';



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

-- bank_clearing_code: table
CREATE TABLE `bank_clearing_code` (
      `id` int NOT NULL AUTO_INCREMENT,
      `region_code` varchar(5) NOT NULL DEFAULT 'MO' COMMENT '地区: MO-澳门, HK-香港, CN-内地',
      `bank_code` varchar(20) NOT NULL COMMENT '银行简码 (如 BOC, BNU, ICBC)',
      `bank_name_cn` varchar(100) NOT NULL COMMENT '银行中文名',
      `bank_name_en` varchar(100) DEFAULT NULL COMMENT '银行英文名/葡文名',
      `logo_url` varchar(255) DEFAULT NULL COMMENT '银行Logo图标',
      `clearing_code` varchar(30) DEFAULT NULL COMMENT '本地清算号 (FPS ID / RTGS Code)',
      `swift_code` varchar(20) DEFAULT NULL COMMENT 'SWIFT BIC Code',
      `is_hot` tinyint DEFAULT '0' COMMENT '是否热门银行: 1-是 0-否',
      `support_channel` varchar(50) DEFAULT 'ALL' COMMENT '支持渠道: FPS,SWIFT (逗号分隔)',
      `status` tinyint DEFAULT '1' COMMENT '1-支持转账 0-维护中',
      `sort_order` int DEFAULT '0' COMMENT '排序权重',
      `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`),
      KEY `idx_region_status` (`region_code`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='银行清算行号数据表';

-- transfer_fee_config: table
CREATE TABLE `transfer_fee_config` (
       `id` int NOT NULL AUTO_INCREMENT,
       `transfer_channel` varchar(30) NOT NULL COMMENT '渠道: INTERNAL, FPS, SWIFT',
       `currency_code` varchar(10) NOT NULL COMMENT '收费币种',
       `user_level` varchar(20) DEFAULT 'ALL' COMMENT '适用客群',
       `fee_type` varchar(10) DEFAULT 'ALL' COMMENT '适用费用承担方式: SHA, OUR, BEN, ALL',
       `fee_currency` varchar(10) DEFAULT 'MOP' COMMENT '手续费扣款币种',
       `deduct_type` tinyint DEFAULT '1' COMMENT '扣费方式: 1-外扣(额外收) 2-内扣(从转账金额中扣除)',
       `calc_mode` tinyint NOT NULL COMMENT '计费模式: 1-固定金额 2-百分比 3-固定+百分比',
       `fixed_amount` decimal(18,2) DEFAULT '0.00' COMMENT '固定费用 (如 100.00)',
       `rate` decimal(10,4) DEFAULT '0.0000' COMMENT '费率 (0.01 表示 1%)',
       `min_fee` decimal(18,2) DEFAULT '0.00' COMMENT '最低收费',
       `max_fee` decimal(18,2) DEFAULT '9999.00' COMMENT '最高收费(封顶)',
       `description` varchar(100) DEFAULT NULL COMMENT '规则描述',
       `status` tinyint DEFAULT '1',
       `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       PRIMARY KEY (`id`),
       KEY `idx_match` (`transfer_channel`,`currency_code`,`user_level`,`fee_type`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转账手续费算费规则';

-- transfer_limit_config: table
CREATE TABLE `transfer_limit_config` (
     `id` int NOT NULL AUTO_INCREMENT,
     `user_level` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '用户等级: NORMAL-普通, KEY-U盾用户, VIP-贵宾',
     `transfer_type` varchar(30) NOT NULL COMMENT '转账类型: INTERNAL, FPS, SWIFT, CROSS_BORDER',
     `currency` varchar(10) NOT NULL DEFAULT 'MOP' COMMENT '限制币种',
     `single_limit` decimal(18,2) NOT NULL COMMENT '单笔限额 (如 50000.00)',
     `daily_limit` decimal(18,2) NOT NULL COMMENT '日累计限额 (如 100000.00)',
     `monthly_limit` decimal(18,2) DEFAULT NULL COMMENT '月累计限额',
     `status` tinyint DEFAULT '1' COMMENT '状态: 1-生效 0-失效',
     `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_rule` (`user_level`,`transfer_type`,`currency`) COMMENT '同一等级同一类型币种只能有一条规则'
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转账限额规则配置表';

-- transfer_schedule: table
CREATE TABLE `transfer_schedule` (
     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
     `user_no` varchar(32) NOT NULL COMMENT '用户编号',
     `schedule_type` tinyint NOT NULL COMMENT '类型: 1-单次预约(Appointment) 2-周期性(Standing Order)',
     `cron_expression` varchar(50) DEFAULT NULL COMMENT '周期规则Cron表达式(如 "0 0 10 1 * ?")',
     `execute_time` datetime NOT NULL COMMENT '下一次执行时间(预约时间)',
     `payer_account_no` varchar(50) NOT NULL COMMENT '付款账号',
     `payee_account_no` varchar(50) NOT NULL COMMENT '收款账号',
     `payee_name` varchar(100) DEFAULT NULL COMMENT '收款户名快照',
     `amount` decimal(18,2) NOT NULL COMMENT '转账金额',
     `currency_code` varchar(10) NOT NULL COMMENT '币种',
     `transfer_channel` varchar(30) NOT NULL DEFAULT 'INTERNAL' COMMENT '渠道',
     `status` tinyint DEFAULT '1' COMMENT '状态: 1-生效中 0-暂停 2-已结束',
     `retry_count` int DEFAULT '0' COMMENT '连续失败次数',
     `remark` varchar(255) DEFAULT NULL COMMENT '备注',
     `last_execute_order_id` varchar(64) DEFAULT NULL COMMENT '最近一次成功生成的订单号(关联 transfer_order.txn_id)',
     `last_execute_status` tinyint DEFAULT NULL COMMENT '上次执行状态',
     `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
     `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     PRIMARY KEY (`id`),
     KEY `idx_execute_status` (`status`,`execute_time`) COMMENT '定时任务扫描索引',
     KEY `idx_user` (`user_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约与周期转账计划表';

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




