package com.macau.bank.transfer.domain.ability;

import com.macau.bank.api.account.response.AccountBalanceRpcResponse;
import com.macau.bank.common.core.enums.AccountStatus;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.transfer.common.result.TransferErrorCode;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.model.AccountSnapshot;
import com.macau.bank.transfer.domain.service.TransferLimitDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class TransferValidator {

    @Resource
    private TransferLimitDomainService limitService;

    /**
     * 统一校验入口
     *
     * @param context 转账上下文
     */
    public void validate(TransferContext context) {
        // 1. 账户状态校验
        AccountSnapshot fromAccount = context.getPayerAccount();
        if (fromAccount == null) {
            throw new BusinessException(TransferErrorCode.FROM_ACCOUNT_NOT_FOUND);
        }
        if (fromAccount.getStatus() != AccountStatus.NORMAL) {
            throw new BusinessException(TransferErrorCode.FROM_ACCOUNT_STATUS_ERROR);
        }

        // 2. 余额校验
        List<AccountBalanceRpcResponse> balances = fromAccount.getBalances();
        BigDecimal balance = CollectionUtils.isEmpty(balances) ? BigDecimal.ZERO
                : balances.stream()
                        .filter(b -> b.getCurrencyCode().equals(context.getOrder().getCurrencyCode()))
                        .findFirst()
                        .map(AccountBalanceRpcResponse::getBalance)
                        .orElse(BigDecimal.ZERO);
        if (balance.compareTo(context.getAmount()) < 0) {
            throw new BusinessException(TransferErrorCode.FROM_ACCOUNT_BALANCE_NOT_ENOUGH);
        }

        // 3. 限额校验
        boolean safe = limitService.checkSingleLimit(
                context.getPayerUserLevel().getCode(),
                context.getOrder().getTransferType().name(),
                context.getOrder().getCurrencyCode(),
                context.getAmount());
        if (!safe) {
            throw new BusinessException(TransferErrorCode.TRANSFER_AMOUNT_EXCEED_LIMIT);
        }
    }
}