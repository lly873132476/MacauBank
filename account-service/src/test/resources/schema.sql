-- 账户信息表 (完整同步 AccountInfo 实体)
-- 注意: Enum 字段使用 INT 存储 ordinal 值
CREATE TABLE IF NOT EXISTS account_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_no VARCHAR(32) NOT NULL,
    account_no VARCHAR(32) NOT NULL UNIQUE,
    card_number VARCHAR(32),
    account_category INT,
    account_type INT,
    status INT DEFAULT 1,
    risk_level VARCHAR(16),
    open_branch_code VARCHAR(16),
    open_branch_name VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 账户余额表
CREATE TABLE IF NOT EXISTS account_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_no VARCHAR(32) NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    balance DECIMAL(18, 2) DEFAULT 0,
    available_balance DECIMAL(18, 2) DEFAULT 0,
    frozen_amount DECIMAL(18, 2) DEFAULT 0,
    total_income DECIMAL(18, 2) DEFAULT 0,
    total_outcome DECIMAL(18, 2) DEFAULT 0,
    version INT DEFAULT 0,
    last_flow_id VARCHAR(64),
    mac_code VARCHAR(128),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (account_no, currency_code)
);

-- 分户账表
CREATE TABLE IF NOT EXISTS account_sub_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_no VARCHAR(64) NOT NULL,
    biz_no VARCHAR(64),
    request_id VARCHAR(64) UNIQUE,
    user_no VARCHAR(32),
    account_no VARCHAR(32) NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    cd_flag VARCHAR(8),
    amount DECIMAL(18, 2) NOT NULL,
    balance DECIMAL(18, 2),
    status VARCHAR(16),
    check_status VARCHAR(16),
    settle_status VARCHAR(16),
    biz_type VARCHAR(32),
    biz_desc VARCHAR(256),
    opponent_info VARCHAR(1024),
    acct_date DATE,
    trans_time TIMESTAMP,
    reconcile_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 冻结日志表
CREATE TABLE IF NOT EXISTS account_freeze_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow_no VARCHAR(64) NOT NULL UNIQUE,
    account_no VARCHAR(32) NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    freeze_type INT,
    reason VARCHAR(256),
    status VARCHAR(16),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unfreeze_time TIMESTAMP
);
