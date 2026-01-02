-- =============================================
-- 澳门银行-外汇-微服务系统数据库脚本
-- 数据库: macau_bank_forex
-- 字符集: UTF-8
-- 时区: Asia/Macau (UTC+8)
-- =============================================

CREATE DATABASE IF NOT EXISTS macau_bank_forex DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE macau_bank_forex;

-- =============================================
-- 1. 货币对交易规则配置表
-- =============================================
CREATE TABLE `currency_pair_config` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `pair_code` varchar(20) NOT NULL COMMENT '交易对代码: HKD_MOP, USD_MOP',
    `base_currency` varchar(10) NOT NULL COMMENT '基准币种',
    `quote_currency` varchar(10) NOT NULL COMMENT '报价币种',
    `pair_name_cn` varchar(50) DEFAULT NULL COMMENT '中文名称: 港币兑澳门元',
    `pair_name_en` varchar(50) DEFAULT NULL COMMENT '英文名称: HKD to MOP',
    `status` tinyint(4) DEFAULT '1' COMMENT '业务开关: 1-开盘 0-停盘 (总闸)',
    `min_amount` decimal(18,2) DEFAULT '100.00' COMMENT '单笔最小交易额',
    `max_amount` decimal(18,2) DEFAULT '1000000.00' COMMENT '单笔最大交易额',
    `rate_precision` tinyint(4) DEFAULT '2' COMMENT '汇率精度',
    `is_pegged` tinyint(4) DEFAULT '0' COMMENT '是否固定汇率: 1-是 0-否',
    `trade_mode` tinyint(4) DEFAULT '1' COMMENT '交易模式: 1-T+0实时 2-T+1交割',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `pair_code` (`pair_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货币对交易规则配置表';


-- =============================================
-- 2. 实时汇率表
-- =============================================
CREATE TABLE `exchange_rate` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT,
     `currency_pair` varchar(20) NOT NULL COMMENT '货币对代码 (如: HKD_MOP, CNY_MOP, USD_MOP)',
     `base_currency` varchar(10) NOT NULL COMMENT '基准货币 (如: HKD)',
     `target_currency` varchar(10) NOT NULL COMMENT '目标货币 (如: MOP)',
     `bank_buy_rate` decimal(24,8) NOT NULL COMMENT '银行买入价 (用户卖出基准货币)',
     `bank_sell_rate` decimal(24,8) NOT NULL COMMENT '银行卖出价 (用户买入基准货币)',
     `middle_rate` decimal(24,8) DEFAULT NULL COMMENT '中间价 (用于内部记账/统计)',
     `unit` int(11) DEFAULT '1' COMMENT '基准货币单位 (通常为1，日元可能为100)',
     `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1-生效 0-失效',
     `effect_time` datetime NOT NULL COMMENT '生效时间',
     `expire_time` datetime DEFAULT NULL COMMENT '失效时间 (新汇率进来时，更新旧记录的失效时间)',
     `source_system` varchar(30) DEFAULT 'BLOOMBERG' COMMENT '来源: BLOOMBERG/REUTERS/MANUAL',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     PRIMARY KEY (`id`),
     KEY `idx_pair_status` (`currency_pair`,`status`),
     KEY `idx_pair_time` (`currency_pair`,`effect_time`,`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实时汇率表';

-- ==========================================
-- 3. 交易订单表 (业务主表)
-- ==========================================
CREATE TABLE `forex_trade_order` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT,
     `txn_id` varchar(64) NOT NULL COMMENT '业务流水号 (全局唯一, 关联 sub_ledger)',
     `request_id` varchar(64) DEFAULT NULL COMMENT '幂等请求ID (前端生成，用于防重复提交)',
     `user_no` varchar(32) NOT NULL COMMENT '用户编号',

    -- 交易核心
     `pair_code` varchar(20) NOT NULL COMMENT '交易对 (如 USD_MOP)',
     `direction` varchar(10) NOT NULL COMMENT '方向: BUY(用户买入/银行卖出), SELL(用户卖出/银行买入)',

    -- 资金 (保留4位小数以支持内部核算)
     `sell_currency` varchar(3) NOT NULL COMMENT '卖出币种',
     `sell_amount` decimal(20,4) NOT NULL COMMENT '卖出金额',
     `buy_currency` varchar(3) NOT NULL COMMENT '买入币种',
     `buy_amount` decimal(20,4) NOT NULL COMMENT '买入金额',

    -- 汇率与利润
     `market_rate` decimal(24,8) NOT NULL COMMENT '市场基准价 (成本)',
     `deal_rate` decimal(24,8) NOT NULL COMMENT '客户成交价 (含点差)',
     `profit_amount_mop` decimal(18,2) DEFAULT '0.00' COMMENT '银行折算利润 (MOP计价)',

    -- 状态
     `status` tinyint(4) DEFAULT '0' COMMENT '0:处理中 1:成功 2:失败 3:冲正(Refounded)',
     `fail_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',

    -- 风控与审计
     `risk_check_pass` tinyint(1) DEFAULT '1' COMMENT '是否通过反洗钱/额度检查',
     `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_txn_id` (`txn_id`),
     UNIQUE KEY `uk_request_id` (`request_id`),
     KEY `idx_user_time` (`user_no`, `create_time`)
) COMMENT='外汇实时交易订单表';

-- ==========================================
-- 4. 银行头寸表 (带并发控制)
-- ==========================================
CREATE TABLE `bank_position` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `currency_code` varchar(3) NOT NULL COMMENT '外币代码 (USD, HKD...)',

    -- 库存核心 (支持 TCC 模式)
     `total_amount` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '总持仓 (包含冻结)',
     `frozen_amount` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '冻结/占用金额 (交易进行中)',
     `average_cost` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '持仓均价 (相对于基准币种 MOP)',

    -- 风控线
     `risk_limit_max` decimal(20,2) NOT NULL COMMENT '持仓上限 (多头限制)',
     `risk_limit_min` decimal(20,2) NOT NULL COMMENT '持仓下限 (空头限制)',

     `status` TINYINT DEFAULT 1 COMMENT '1:正常 0:暂停交易(熔断)',

    -- 乐观锁
     `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_currency` (`currency_code`)
) COMMENT='银行自营头寸表';

-- ==========================================
-- 5. 报价快照表 (审计专用)
-- ==========================================
CREATE TABLE `forex_quote_log` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `txn_id` varchar(64) NOT NULL COMMENT '关联交易单号',

    -- 报价快照
   `quote_time` datetime(3) NOT NULL COMMENT '报价时间(精确到毫秒)',
   `pair_code` varchar(20) NOT NULL,
   `ask_price` decimal(24,8) NOT NULL COMMENT '银行卖出价',
   `bid_price` decimal(24,8) NOT NULL COMMENT '银行买入价',
   `mid_price` decimal(24,8) NOT NULL COMMENT '中间价',

    -- 溯源信息
   `source_system` varchar(20) DEFAULT 'BLOOMBERG' COMMENT '报价来源',
   `spread_version` varchar(20) DEFAULT NULL COMMENT '点差配置版本号',

   PRIMARY KEY (`id`),
   KEY `idx_txn` (`txn_id`)
) COMMENT='交易报价审计日志表';



