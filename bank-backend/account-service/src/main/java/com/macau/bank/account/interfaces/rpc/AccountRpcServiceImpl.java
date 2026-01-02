package com.macau.bank.account.interfaces.rpc;

import com.macau.bank.account.application.assembler.AccountDomainAssembler;
import com.macau.bank.account.application.command.AdjustBalanceCmd;
import com.macau.bank.account.application.command.CreateAccountCmd;
import com.macau.bank.account.application.command.CreditCmd;
import com.macau.bank.account.application.command.DebitCmd;
import com.macau.bank.account.application.command.FreezeBalanceCmd;
import com.macau.bank.account.application.command.UnfreezeAndDebitCmd;
import com.macau.bank.account.application.command.UnfreezeBalanceCmd;
import com.macau.bank.account.application.query.AccountListQuery;
import com.macau.bank.account.application.query.AccountSummaryQuery;
import com.macau.bank.account.application.query.AssetSummaryQuery;
import com.macau.bank.account.application.result.AccountInfoResult;
import com.macau.bank.account.application.result.AssetSummaryResult;
import com.macau.bank.account.application.service.AccountAppService;
import com.macau.bank.account.domain.entity.AccountBalance;
import com.macau.bank.account.domain.entity.AccountInfo;
import com.macau.bank.account.domain.service.AccountDomainService;
import com.macau.bank.account.interfaces.rpc.assembler.AccountRpcAssembler;
import com.macau.bank.api.account.request.AdjustBalanceRpcRequest;
import com.macau.bank.api.account.request.CreateAccountRpcRequest;
import com.macau.bank.api.account.request.CreditRpcRequest;
import com.macau.bank.api.account.request.DebitRpcRequest;
import com.macau.bank.api.account.request.FreezeBalanceRpcRequest;
import com.macau.bank.api.account.request.UnfreezeAndDebitRpcRequest;
import com.macau.bank.api.account.request.UnfreezeBalanceRpcRequest;
import com.macau.bank.api.account.response.AccountInfoRpcResponse;
import com.macau.bank.api.account.response.AssetSummaryRpcResponse;
import com.macau.bank.api.account.service.AccountRpcService;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

/**
 * 账户Dubbo服务实现
 */
@Slf4j
@DubboService
public class AccountRpcServiceImpl implements AccountRpcService {

    @Resource
    private AccountAppService accountAppService;

    @Resource
    private AccountRpcAssembler accountRpcAssembler;

    @Resource
    private AccountDomainService accountDomainService;

    @Resource
    private AccountDomainAssembler accountDomainAssembler;

    @Override
    public Result<AssetSummaryRpcResponse> getAssetSummary(String userNo) {
        return executeRpc(() -> {
            AssetSummaryQuery query = AssetSummaryQuery.builder().userNo(userNo).build();
            AssetSummaryResult result = accountAppService.getAssetSummary(query);
            return accountRpcAssembler.toRpc(result);
        }, "获取资产总览", userNo);
    }

    @Override
    public Result<List<AccountInfoRpcResponse>> getAccountList(String userNo) {
        return executeRpc(() -> {
            AccountListQuery query = AccountListQuery.builder().userNo(userNo).build();
            List<AccountInfoResult> resultList = accountAppService.getAccountList(query);
            return accountRpcAssembler.toRpcList(resultList);
        }, "获取账户列表", userNo);
    }

    @Override
    public Result<AccountInfoRpcResponse> getAccountSummaryByAccountNo(String accountNo) {
        return executeRpc(() -> {
            AccountSummaryQuery query = new AccountSummaryQuery();
            query.setAccountNo(accountNo);
            AccountInfoResult result = accountAppService.getAccountSummary(query);
            return accountRpcAssembler.toRpc(result);
        }, "获取账户详情", accountNo);
    }

    @Override
    @Deprecated
    public Result<Boolean> adjustBalance(AdjustBalanceRpcRequest request) {
        return executeRpc(() -> {
            AdjustBalanceCmd cmd = accountRpcAssembler.toCmd(request);
            accountAppService.adjustBalance(cmd);
            return true;
        }, "调整余额", request);
    }

    @Override
    public Result<Boolean> debit(DebitRpcRequest request) {
        return executeRpc(() -> {
            DebitCmd cmd = accountRpcAssembler.toCmd(request);
            accountAppService.debit(cmd);
            return true;
        }, "扣款", request);
    }

    @Override
    public Result<Boolean> credit(CreditRpcRequest request) {
        return executeRpc(() -> {
            CreditCmd cmd = accountRpcAssembler.toCmd(request);
            accountAppService.credit(cmd);
            return true;
        }, "入账", request);
    }

    @Override
    public Result<BigDecimal> getAvailableBalance(String accountNo, String currencyCode) {
        return executeRpc(() -> accountAppService.getAvailableBalance(accountNo, currencyCode),
                "获取可用余额", accountNo + ":" + currencyCode);
    }

    @Override
    public Result<BigDecimal> getTotalBalance(String accountNo, String currencyCode) {
        return executeRpc(() -> accountAppService.getTotalBalance(accountNo, currencyCode),
                "获取总余额", accountNo + ":" + currencyCode);
    }

    @Override
    public Result<String> getAccountNoByUserNoAndCurrency(String userNo, String currencyCode) {
        return executeRpc(() -> accountAppService.getAccountNoByUserNoAndCurrency(userNo, currencyCode),
                "获取账户号", userNo + ":" + currencyCode);
    }

    @Override
    public Result<String> createAccount(CreateAccountRpcRequest request) {
        return executeRpc(() -> {
            CreateAccountCmd cmd = accountRpcAssembler.toCmd(request);
            return accountAppService.createAccount(cmd);
        }, "创建账户", request);
    }

    @Override
    public Result<AccountInfoRpcResponse> getAssetsByCardNumber(String cardNumber) {
        return executeRpc(() -> {
            if (!StringUtils.hasText(cardNumber)) {
                return null;
            }

            AccountInfo accountInfo = accountDomainService.getAccountInfoByCardNumber(cardNumber);
            if (accountInfo == null) {
                return null;
            }

            List<AccountBalance> balances = accountDomainService
                    .getAccountBalancesByAccountNo(accountInfo.getAccountNo());

            AccountInfoResult result = accountDomainAssembler.toResult(accountInfo);
            result.setBalances(accountDomainAssembler.toBalanceList(balances));

            return accountRpcAssembler.toRpc(result);
        }, "根据卡号获取资产", cardNumber);
    }

    @Override
    public Result<Boolean> freezeBalance(FreezeBalanceRpcRequest request) {
        return executeRpc(() -> {
            FreezeBalanceCmd cmd = accountRpcAssembler.toCmd(request);
            return accountAppService.freezeBalance(cmd);
        }, "冻结余额", request);
    }

    @Override
    public Result<Boolean> unfreezeBalance(UnfreezeBalanceRpcRequest request) {
        return executeRpc(() -> {
            UnfreezeBalanceCmd cmd = accountRpcAssembler.toCmd(request);
            return accountAppService.unfreezeBalance(cmd);
        }, "解冻余额", request);
    }

    @Override
    public Result<Boolean> unfreezeAndDebit(UnfreezeAndDebitRpcRequest request) {
        return executeRpc(() -> {
            UnfreezeAndDebitCmd cmd = accountRpcAssembler.toCmd(request);
            return accountAppService.unfreezeAndDebit(cmd);
        }, "解冻并扣款", request);
    }

    @Override
    public Result<Boolean> validateAccountOwnership(String accountNo, String userNo) {
        return executeRpc(() -> {
            if (accountNo == null || userNo == null) {
                return false;
            }
            AccountSummaryQuery query = new AccountSummaryQuery();
            query.setAccountNo(accountNo);
            AccountInfoResult result = accountAppService.getAccountSummary(query);
            return result != null && userNo.equals(result.getUserNo());
        }, "校验账户归属", accountNo + ":" + userNo);
    }

    /**
     * 统一的 RPC 执行模板
     * <p>
     * 作用：
     * 1. 统一异常处理（BusinessException 返回错误码，Exception 返回 500）
     * 2. 统一日志记录（业务失败记录 WARN，系统异常记录 ERROR）
     * 3. 统一返回格式（Result 包装）
     *
     * @param action    业务逻辑
     * @param operation 操作描述（用于日志）
     * @param request   请求参数（用于日志）
     * @return Result 包装的结果
     */
    private <T> Result<T> executeRpc(Supplier<T> action, String operation, Object request) {
        try {
            T result = action.get();
            return Result.success(result);
        } catch (BusinessException e) {
            log.warn("RPC {}业务失败: request={}, code={}, msg={}",
                    operation, request, e.getCode(), e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("RPC {}系统异常: request={}", operation, request, e);
            return Result.fail(500, "系统繁忙，请稍后重试");
        }
    }
}