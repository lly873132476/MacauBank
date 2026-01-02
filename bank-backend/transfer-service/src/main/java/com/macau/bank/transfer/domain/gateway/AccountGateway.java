package com.macau.bank.transfer.domain.gateway;

import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.transfer.domain.model.AccountSnapshot;

import java.math.BigDecimal;

/**
 * 账户网关接口
 * 归属：Domain 层
 * 作用：策略类只认这个接口，不认 Dubbo
 */
public interface AccountGateway {

        /**
         * 根据账户号查询账户信息
         *
         * @param accountNo 账户号
         * @return 账户快照，不存在时返回 null
         */
        AccountSnapshot getAccount(String accountNo);

        /**
         * 根据银行卡号查询账户信息
         *
         * @param accountNo 银行卡号
         * @return 账户快照，不存在时返回 null
         */
        AccountSnapshot getAccountByCardNo(String accountNo);

        /**
         * 扣款操作
         *
         * @param accountNo    账户号
         * @param currencyCode 币种代码
         * @param amount       扣款金额（正数）
         * @param description  业务描述
         * @param bizNo        业务流水号
         * @param requestId    幂等请求ID
         * @return 操作是否成功
         */
        boolean debit(String accountNo, String currencyCode, BigDecimal amount, String description, String bizNo,
                        String requestId);

        /**
         * 入账操作
         *
         * @param accountNo    账户号
         * @param currencyCode 币种代码
         * @param amount       入账金额（正数）
         * @param description  业务描述
         * @param bizNo        业务流水号
         * @param requestId    幂等请求ID
         * @return 操作是否成功
         */
        boolean credit(String accountNo, String currencyCode, BigDecimal amount, String description, String bizNo,
                        String requestId);

        /**
         * 冻结账户余额
         *
         * @param accountNo    账户号
         * @param currencyCode 币种代码
         * @param amount       冻结金额
         * @param flowNo       业务流水号（用于解冻时关联）
         * @param reason       冻结原因
         * @return 操作是否成功
         */
        boolean freeze(String accountNo, String currencyCode, BigDecimal amount, String flowNo, String reason);

        /**
         * 解冻账户余额
         *
         * @param accountNo    账户号
         * @param currencyCode 币种代码
         * @param amount       解冻金额
         * @param flowNo       业务流水号（与冻结时一致）
         * @param reason       解冻原因
         * @return 操作是否成功
         */
        boolean unFreeze(String accountNo, String currencyCode, BigDecimal amount, String flowNo, String reason);

        /**
         * 解冻并扣款（原子操作）
         * <p>
         * 场景：转账冻结 → 确认成功 → 解冻并扣款
         */
        boolean unfreezeAndDebit(String accountNo, String currencyCode, BigDecimal amount, String flowNo,
                        String reason, BizType bizType, String requestId);
}
