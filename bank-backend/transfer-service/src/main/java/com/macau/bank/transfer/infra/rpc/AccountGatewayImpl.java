package com.macau.bank.transfer.infra.rpc;

import com.macau.bank.api.account.request.CreditRpcRequest;
import com.macau.bank.api.account.request.DebitRpcRequest;
import com.macau.bank.api.account.request.FreezeBalanceRpcRequest;
import com.macau.bank.api.account.request.UnfreezeBalanceRpcRequest;
import com.macau.bank.api.account.response.AccountInfoRpcResponse;
import com.macau.bank.api.account.service.AccountRpcService;
import com.macau.bank.common.core.enums.FreezeType;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.exception.SystemException;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.api.account.request.UnfreezeAndDebitRpcRequest;
import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.model.AccountSnapshot;
import com.macau.bank.transfer.infra.rpc.converter.AccountRpcConverter;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 账户网关实现（防腐层）
 * <p>
 * 职责：
 * 1. 调用 account-service 的 RPC 接口
 * 2. 将 RPC Response 转换为 Domain 模型
 * 3. 统一处理 RPC 调用结果
 */
@Component
@Slf4j
public class AccountGatewayImpl implements AccountGateway {

    @DubboReference
    private AccountRpcService accountRpcService;

    @Resource
    private AccountRpcConverter accountRpcConverter;

    @Override
    public AccountSnapshot getAccount(String accountNo) {
        Result<AccountInfoRpcResponse> result = accountRpcService.getAccountSummaryByAccountNo(accountNo);
        AccountInfoRpcResponse response = handleRpcResult(result, "查询账户: " + accountNo, false);
        return response != null ? accountRpcConverter.toSnapshot(response) : null;
    }

    @Override
    public AccountSnapshot getAccountByCardNo(String cardNo) {
        Result<AccountInfoRpcResponse> result = accountRpcService.getAssetsByCardNumber(cardNo);
        AccountInfoRpcResponse response = handleRpcResult(result, "查询账户(卡号): " + cardNo, false);
        return response != null ? accountRpcConverter.toSnapshot(response) : null;
    }

    @Override
    public boolean debit(String accountNo, String currencyCode, BigDecimal amount, String description, String bizNo,
            String requestId) {
        DebitRpcRequest request = DebitRpcRequest.builder()
                .accountNo(accountNo)
                .currencyCode(currencyCode)
                .amount(amount)
                .description(description)
                .bizNo(bizNo)
                .requestId(requestId)
                .build();

        Result<Boolean> result = accountRpcService.debit(request);
        return handleRpcResult(result, "扣款", true);
    }

    @Override
    public boolean credit(String accountNo, String currencyCode, BigDecimal amount, String description, String bizNo,
            String requestId) {
        CreditRpcRequest request = CreditRpcRequest.builder()
                .accountNo(accountNo)
                .currencyCode(currencyCode)
                .amount(amount)
                .description(description)
                .bizNo(bizNo)
                .requestId(requestId)
                .build();

        Result<Boolean> result = accountRpcService.credit(request);
        return handleRpcResult(result, "入账", true);
    }

    @Override
    public boolean freeze(String accountNo, String currencyCode, BigDecimal amount, String flowNo, String reason) {
        FreezeBalanceRpcRequest request = FreezeBalanceRpcRequest.builder()
                .accountNo(accountNo)
                .currencyCode(currencyCode)
                .amount(amount)
                .flowNo(flowNo)
                .freezeType(FreezeType.TRANSACTION)
                .reason(reason)
                .build();

        Result<Boolean> result = accountRpcService.freezeBalance(request);
        return handleRpcResult(result, "冻结余额", true);
    }

    @Override
    public boolean unFreeze(String accountNo, String currencyCode, BigDecimal amount, String flowNo, String reason) {
        UnfreezeBalanceRpcRequest request = UnfreezeBalanceRpcRequest.builder()
                .accountNo(accountNo)
                .currencyCode(currencyCode)
                .amount(amount)
                .flowNo(flowNo)
                .reason(reason)
                .build();

        Result<Boolean> result = accountRpcService.unfreezeBalance(request);
        return handleRpcResult(result, "解冻余额", true);
    }

    @Override
    public boolean unfreezeAndDebit(String accountNo, String currencyCode, BigDecimal amount, String flowNo,
            String reason, BizType bizType, String requestId) {
        UnfreezeAndDebitRpcRequest request = UnfreezeAndDebitRpcRequest.builder()
                .accountNo(accountNo)
                .currencyCode(currencyCode)
                .amount(amount)
                .flowNo(flowNo)
                .reason(reason)
                .bizType(bizType)
                .requestId(requestId)
                .build();

        Result<Boolean> result = accountRpcService.unfreezeAndDebit(request);
        return handleRpcResult(result, "解冻并扣款", true);
    }

    /**
     * 处理 RPC 调用结果（泛型版本）
     *
     * @param result         RPC 调用结果
     * @param operation      操作描述
     * @param throwOnFailure true=失败抛异常，false=失败返回 null
     * @return 数据，失败时根据 throwOnFailure 决定抛异常或返回 null
     */
    private <T> T handleRpcResult(@Nullable Result<T> result, String operation, boolean throwOnFailure) {
        // 1. 判空处理
        if (result == null) {
            if (throwOnFailure) {
                log.error("账户服务返回为空: operation={}", operation);
                throw new SystemException("账户服务返回为空");
            }
            log.warn("RPC 调用返回空: operation={}", operation);
            return null;
        }

        // 2. 判断是否成功
        if (!result.isSuccess()) {
            if (throwOnFailure) {
                int code = result.getCode();
                if (code == 500) {
                    log.error("账户服务系统异常: operation={}, msg={}", operation, result.getMessage());
                    throw new SystemException("账户服务不可用: " + result.getMessage());
                }
                throw new BusinessException(code, result.getMessage());
            }
            log.warn("RPC 调用失败: operation={}, code={}, msg={}", operation, result.getCode(), result.getMessage());
            return null;
        }

        // 3. 返回数据
        T data = result.getData();
        if (data == null && !throwOnFailure) {
            log.warn("RPC 调用成功但数据为空: operation={}", operation);
        }
        return data;
    }
}
