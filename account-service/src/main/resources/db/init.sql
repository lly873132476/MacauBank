-- =============================================
-- 澳门银行-账户-微服务系统数据库脚本
-- 数据库: macau_bank_account
-- 字符集: UTF-8
-- 时区: Asia/Macau (UTC+8)
-- =============================================

CREATE DATABASE IF NOT EXISTS macau_bank_account DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE macau_bank_account;

-- =============================================
-- 1. 货币定义表
-- =============================================
CREATE TABLE `account_currency_config` (
       `id` INT AUTO_INCREMENT PRIMARY KEY,
       `currency_code` VARCHAR(3) NOT NULL UNIQUE COMMENT '币种代码: MOP, HKD, CNY, USD',
       `currency_name` VARCHAR(20) NOT NULL COMMENT '名称: 澳门元, 港币, 人民币',
       `currency_symbol` VARCHAR(5) DEFAULT '$' COMMENT '符号',
       `is_local` TINYINT DEFAULT 0 COMMENT '是否本地货币: 1-是(MOP) 0-否',
       `sort_order` INT DEFAULT 0 COMMENT '排序优先级: MOP排第一',
       `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-停用',

        -- 审计字段必须成对出现
       `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
       `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间(如状态变更)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户支持币种配置表';

-- 初始化数据示例（澳门银行特色）
INSERT INTO account_currency_config (currency_code, currency_name, is_local) VALUES ('MOP', '澳门元', 1);
INSERT INTO account_currency_config (currency_code, currency_name, is_local) VALUES ('HKD', '港币', 0);


-- =============================================
-- 2. 账户主档表
-- =============================================
CREATE TABLE `account_info` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_no` VARCHAR(32) NOT NULL COMMENT '关联用户中心的用户编号',
    `account_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '银行账号(核心主键, 比如 12位数字)',

    -- 账户属性
    `card_number` VARCHAR(32) UNIQUE COMMENT '关联的借记卡卡号(如果有)',
    `account_category` TINYINT DEFAULT 1 COMMENT '账户大类: 1-个人户 2-企业户',
    `account_type` TINYINT DEFAULT 1 COMMENT '账户类型: 1-多币种储蓄(Multi-Currency Savings) 2-往来户(Current)',

    -- 状态管理
    `status` TINYINT DEFAULT 1 COMMENT '账户状态: 1-正常 2-冻结(只进不出) 3-全封锁(不进不出) 4-注销',
    `risk_level` TINYINT DEFAULT 1 COMMENT '风险等级(反洗钱用)',

    -- 开户行信息
    `open_branch_code` VARCHAR(20) DEFAULT 'HEAD_OFFICE' COMMENT '开户网点代码',
    `open_branch_name` VARCHAR(50) DEFAULT '总行营业部' COMMENT '开户网点名称',

    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开户时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user_no (`user_no`),
    INDEX idx_card_number (`card_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户主档表';


-- =============================================
-- 3. 币种账户表
-- =============================================
CREATE TABLE `account_balance` (
   `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
   `account_no` VARCHAR(32) NOT NULL COMMENT '关联 account_info.account_no',
   `currency_code` VARCHAR(3) NOT NULL COMMENT '币种: MOP, HKD, CNY',

    -- 核心金额字段 (使用 DECIMAL 保证金融精度)
   `balance` DECIMAL(18, 2) NOT NULL DEFAULT 0.00 COMMENT '当前余额 (包含冻结金额)',
   `available_balance` DECIMAL(18, 2) NOT NULL DEFAULT 0.00 COMMENT '可用余额 (可提现/转账的钱)',
   `frozen_amount` DECIMAL(18, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额 (如: 司法冻结、预授权)',

    -- 累计字段 (可选，用于风控或统计)
   `total_income` DECIMAL(18, 2) DEFAULT 0.00 COMMENT '历史总入账',
   `total_outcome` DECIMAL(18, 2) DEFAULT 0.00 COMMENT '历史总出账',

    -- 安全字段
   `version` INT DEFAULT 0 COMMENT '乐观锁版本号 (防止并发扣款)',
   `last_flow_id` VARCHAR(64) COMMENT '最后一笔变动的流水号(对账用)',
   `mac_code` VARCHAR(128) COMMENT '余额防篡改校验码(HMAC, 防止直接改库)',

    -- 审计字段：成对出现
   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次开通该币种的时间',
   `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 联合唯一索引: 确保一个账号下，每种货币只有一行记录
   UNIQUE KEY uk_acc_curr (`account_no`, `currency_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户分币种余额表';


-- =============================================
-- 4. 冻结/解冻明细表
-- =============================================
-- account_freeze_log: table
CREATE TABLE `account_freeze_log` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `flow_no` varchar(64) NOT NULL COMMENT '冻结流水号',
      `account_no` varchar(32) NOT NULL,
      `currency_code` varchar(3) NOT NULL,
      `amount` decimal(18,2) NOT NULL COMMENT '冻结金额',
      `freeze_type` tinyint DEFAULT NULL COMMENT '类型: 1-交易冻结 2-司法冻结 3-错账冻结',
      `reason` varchar(255) DEFAULT NULL COMMENT '冻结原因',
      `status` tinyint DEFAULT '0' COMMENT '状态: 0-已冻结 1-已解冻 2-已解冻',
      `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
      `unfreeze_time` datetime DEFAULT NULL COMMENT '解冻时间',
      PRIMARY KEY (`id`),
      UNIQUE KEY `flow_no` (`flow_no`),
      KEY `idx_acc_curr` (`account_no`,`currency_code`),
      KEY `idx_status_create_time` (`status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资金冻结记录表';


-- =============================================
-- 5. 账户分户账明细表
-- =============================================
CREATE TABLE `account_sub_ledger` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `voucher_no` varchar(64) NOT NULL COMMENT '会计凭证号(全局唯一)',
      `biz_no` varchar(64) NOT NULL COMMENT '业务流水号',
      `request_id` varchar(64) DEFAULT NULL COMMENT '幂等请求ID(前端生成，用于防重复提交)',
      `user_no` varchar(32) NOT NULL COMMENT '冗余字段：用户编号(分库分表键)',

    -- 1. 核心会计分录
      `account_no` varchar(32) NOT NULL COMMENT '账号',
      `currency_code` varchar(3) NOT NULL COMMENT '币种',
      `cd_flag` char(1) NOT NULL COMMENT '借贷标志: D-借(Debit,出), C-贷(Credit,入)',
      `amount` decimal(18,2) NOT NULL COMMENT '发生额(绝对值)',
      `balance` decimal(18,2) NOT NULL COMMENT '变动后余额(Snapshot)',

    -- 2. 状态字段 (你觉得少的地方，都在这里)
      `status` tinyint DEFAULT '1' COMMENT '会计状态: 1-正常入账 2-红字冲正(Reversed) 3-蓝字补账',
      `check_status` tinyint DEFAULT '0' COMMENT '对账状态: 0-未对账 1-对账平 2-对账不平(存疑)',
      `settle_status` tinyint DEFAULT '1' COMMENT '结算状态: 1-实时入账 2-日终批处理 3-挂账(Pending)',

    -- 3. 业务上下文 (冗余存储，方便打印流水单)
      `biz_type` varchar(30) NOT NULL COMMENT '业务类型: TRANSFER_OUT/DEPOSIT/FEE/INTEREST',
      `biz_desc` varchar(255) DEFAULT NULL COMMENT '摘要: 转账给陈大文',
      `opponent_info` json DEFAULT NULL COMMENT '对手方信息快照 {"name": "陈大文", "account": "888..."}',

    -- 4. 关键时间
      `acct_date` date NOT NULL COMMENT '会计日期 (核心！决定这就交易算在今天的账还是明天的账)',
      `trans_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '实际发生时间',
      `reconcile_time` datetime DEFAULT NULL COMMENT '对账完成时间',
      PRIMARY KEY (`id`),
      UNIQUE KEY `voucher_no` (`voucher_no`),
      KEY `idx_biz_no` (`biz_no`),
      UNIQUE KEY `uk_request_id` (`request_id`),
      KEY `idx_acc_date` (`account_no`,`acct_date`),
      KEY `idx_user_time` (`user_no`,`trans_time`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账户分户账明细表(Sub-ledger)';