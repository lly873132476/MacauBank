-- 简单的转账流水表（模拟）
CREATE TABLE IF NOT EXISTS `transfer_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `txn_id` varchar(64) NOT NULL COMMENT '交易流水号',
  `from_account_no` varchar(32) NOT NULL COMMENT '付款方账号',
  `to_account_no` varchar(32) NOT NULL COMMENT '收款方账号',
  `amount` decimal(18,2) NOT NULL COMMENT '金额',
  `currency_code` varchar(3) NOT NULL COMMENT '币种',
  `transfer_type` varchar(32) NOT NULL COMMENT '转账类型',
  `status` varchar(32) NOT NULL COMMENT '状态',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_txn_id` (`txn_id`)
);
