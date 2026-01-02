package com.macau.bank.account.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.account.application.command.*;
import com.macau.bank.account.application.query.AccountListQuery;
import com.macau.bank.account.application.query.AccountSummaryQuery;
import com.macau.bank.account.application.query.AssetSummaryQuery;
import com.macau.bank.account.application.query.TransactionFlowQuery;
import com.macau.bank.account.application.result.AccountInfoResult;
import com.macau.bank.account.application.result.AssetSummaryResult;
import com.macau.bank.account.application.result.TransactionFlowResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户应用服务接口
 */
public interface AccountAppService {

    /**
     * 获取资产总览
     */
    AssetSummaryResult getAssetSummary(AssetSummaryQuery query);

    /**
     * 获取账户列表
     */
    List<AccountInfoResult> getAccountList(AccountListQuery query);

    /**
     * 获取账户详情
     */
    AccountInfoResult getAccountSummary(AccountSummaryQuery query);

    /**
     * 获取可用余额
     */
    BigDecimal getAvailableBalance(String accountNo, String currencyCode);

    /**
     * 获取总余额
     */
    BigDecimal getTotalBalance(String accountNo, String currencyCode);

    /**
     * 根据用户和币种查询账户号
     */
    String getAccountNoByUserNoAndCurrency(String userNo, String currencyCode);

    /**
     * 创建账户
     */
    String createAccount(CreateAccountCmd cmd);

    /**
     * 调整余额
     * 
     * @deprecated 请使用 {@link #debit(DebitCmd)} 或 {@link #credit(CreditCmd)}
     */
    @Deprecated
    boolean adjustBalance(AdjustBalanceCmd cmd);

    /**
     * 扣款
     */
    boolean debit(DebitCmd cmd);

    /**
     * 入账
     */
    boolean credit(CreditCmd cmd);

    /**
     * 冻结余额
     */
    boolean freezeBalance(FreezeBalanceCmd cmd);

    /**
     * 解冻余额
     */
    boolean unfreezeBalance(UnfreezeBalanceCmd cmd);

    /**
     * 解冻并扣款（原子操作）
     * <p>
     * 场景：转账冻结 → 确认成功 → 解冻并扣款
     */
    boolean unfreezeAndDebit(UnfreezeAndDebitCmd cmd);

    /**
     * 分页获取交易流水
     */
    Page<TransactionFlowResult> getTransactionFlows(TransactionFlowQuery query);

    /**
     * 管理员充值 (仅限后台)
     */
    boolean adminDeposit(AdminDepositCmd cmd);

    /**
     * 开通币种账户 (后台管理)
     */
    boolean openCurrencyAccount(AdminOpenCurrencyAccountCmd cmd);

    /**
     * 校验账户是否属于指定用户
     * 
     * @param accountNo 账户号
     * @param userNo    用户编号
     * @return true-属于该用户，false-不属于
     */
    boolean validateAccountOwnership(String accountNo, String userNo);
}
