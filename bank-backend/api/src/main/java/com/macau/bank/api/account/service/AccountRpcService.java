package com.macau.bank.api.account.service;

import com.macau.bank.api.account.request.AdjustBalanceRpcRequest;
import com.macau.bank.api.account.request.CreateAccountRpcRequest;
import com.macau.bank.api.account.request.CreditRpcRequest;
import com.macau.bank.api.account.request.DebitRpcRequest;
import com.macau.bank.api.account.request.FreezeBalanceRpcRequest;
import com.macau.bank.api.account.request.UnfreezeAndDebitRpcRequest;
import com.macau.bank.api.account.request.UnfreezeBalanceRpcRequest;
import com.macau.bank.api.account.response.AccountInfoRpcResponse;
import com.macau.bank.api.account.response.AssetSummaryRpcResponse;
import com.macau.bank.common.core.result.Result;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户 Dubbo 服务接口
 */
public interface AccountRpcService {

    /**
     * 获取资产总览
     *
     * @param userNo 用户编号
     */
    Result<AssetSummaryRpcResponse> getAssetSummary(String userNo);

    /**
     * 获取账户列表
     *
     * @param userNo 用户编号
     */
    Result<List<AccountInfoRpcResponse>> getAccountList(String userNo);

    /**
     * 根据账户号查询账户汇总信息
     *
     * @param accountNo 账户号
     */
    Result<AccountInfoRpcResponse> getAccountSummaryByAccountNo(String accountNo);

    /**
     * 调整余额 (入账/出账)
     *
     * @param request 调整余额请求
     * @deprecated 请使用 {@link #debit(DebitRpcRequest)} 或
     *             {@link #credit(CreditRpcRequest)}
     */
    @Deprecated
    Result<Boolean> adjustBalance(AdjustBalanceRpcRequest request);

    /**
     * 扣款
     *
     * @param request 扣款请求 (金额必须为正数)
     */
    Result<Boolean> debit(DebitRpcRequest request);

    /**
     * 入账
     *
     * @param request 入账请求 (金额必须为正数)
     */
    Result<Boolean> credit(CreditRpcRequest request);

    /**
     * 获取可用余额
     *
     * @param accountNo    账户号
     * @param currencyCode 币种代码
     */
    Result<BigDecimal> getAvailableBalance(String accountNo, String currencyCode);

    /**
     * 获取总余额
     *
     * @param accountNo    账户号
     * @param currencyCode 币种代码
     */
    Result<BigDecimal> getTotalBalance(String accountNo, String currencyCode);

    /**
     * 根据用户编号和币种获取账户号
     *
     * @param userNo       用户编号
     * @param currencyCode 币种代码
     */
    Result<String> getAccountNoByUserNoAndCurrency(String userNo, String currencyCode);

    /**
     * 校验账户是否属于指定用户
     *
     * @param accountNo 账户号
     * @param userNo    用户编号
     * @return true-属于该用户，false-不属于
     */
    Result<Boolean> validateAccountOwnership(String accountNo, String userNo);

    /**
     * 创建账户
     *
     * @param request 创建账户请求
     */
    Result<String> createAccount(CreateAccountRpcRequest request);

    /**
     * 根据卡号获取用户资产信息
     *
     * @param cardNumber 银行卡号
     * @return 账户信息及余额列表
     */
    Result<AccountInfoRpcResponse> getAssetsByCardNumber(String cardNumber);

    /**
     * 冻结账户余额
     *
     * @param request 冻结余额请求
     */
    Result<Boolean> freezeBalance(FreezeBalanceRpcRequest request);

    /**
     * 解冻账户余额
     *
     * @param request 解冻余额请求
     */
    Result<Boolean> unfreezeBalance(UnfreezeBalanceRpcRequest request);

    /**
     * 解冻并扣款（原子操作）
     * <p>
     * 场景：转账冻结 → 确认成功 → 解冻并扣款
     *
     * @param request 解冻并扣款请求
     */
    Result<Boolean> unfreezeAndDebit(UnfreezeAndDebitRpcRequest request);
}