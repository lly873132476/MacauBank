-- 测试数据初始化

-- 清空现有数据 (确保幂等性)
TRUNCATE TABLE account_sub_ledger;
TRUNCATE TABLE account_balance;
TRUNCATE TABLE account_freeze_log;
TRUNCATE TABLE account_info;

-- 账户信息
-- PERSONAL=1, SAVINGS=1, NORMAL=1
INSERT INTO account_info (user_no, account_no, card_number, account_category, account_type, status) VALUES
('USER_001', 'ACC_001', '6228001001', 1, 1, 1),
('USER_002', 'ACC_002', '6228001002', 1, 1, 1);

-- 账户余额 - 张三有 10000 MOP
INSERT INTO account_balance (account_no, currency_code, balance, available_balance, frozen_amount, total_income, total_outcome, version) VALUES
('ACC_001', 'MOP', 10000.00, 10000.00, 0.00, 10000.00, 0.00, 0),
('ACC_002', 'MOP', 5000.00, 5000.00, 0.00, 5000.00, 0.00, 0);
