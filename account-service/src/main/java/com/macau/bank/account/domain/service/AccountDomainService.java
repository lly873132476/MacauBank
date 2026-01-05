package com.macau.bank.account.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.macau.bank.account.common.result.AccountErrorCode;
import com.macau.bank.account.domain.entity.*;
import com.macau.bank.account.domain.model.UserAssetView;
import com.macau.bank.account.domain.repository.*;
import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.*;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.sequence.AccountSequenceGenerator;
import com.macau.bank.common.sequence.CardNumberGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 账户领域服务
 * <p>
 * 职责：账户信息管理和查询
 * - 账户创建
 * - 币种账户开通
 * - 账户信息查询
 * - 资产视图构建
 * <p>
 * 注：余额操作相关逻辑已拆分至 {@link AccountBalanceDomainService}
 */
@Slf4j
@Service
public class AccountDomainService {

    @Resource
    private AccountInfoRepository accountInfoRepository;

    @Resource
    private AccountBalanceRepository accountBalanceRepository;

    @Resource
    private AccountSubLedgerRepository accountSubLedgerRepository;

    @Resource
    private AccountCurrencyConfigRepository accountCurrencyConfigRepository;

    @Resource
    private AccountSequenceGenerator accountSequenceGenerator;

    @Resource
    private CardNumberGenerator cardNumberGenerator;

    // ==================== 账户信息查询 ====================

    public List<AccountInfo> getAccountInfosByUserNo(String userNo) {
        return accountInfoRepository.findByUserNo(userNo);
    }

    public AccountInfo getAccountInfoByAccountNo(String accountNo) {
        return accountInfoRepository.findByAccountNo(accountNo);
    }

    public AccountInfo getAccountInfoByCardNumber(String cardNumber) {
        return accountInfoRepository.findByCardNumber(cardNumber);
    }

    // ==================== 余额查询 ====================

    public List<AccountBalance> getAccountBalancesByAccountNo(String accountNo) {
        return accountBalanceRepository.findByAccountNo(accountNo);
    }

    public List<AccountBalance> getBalancesByUserNo(String userNo) {
        List<AccountInfo> accounts = getAccountInfosByUserNo(userNo);
        if (accounts == null || accounts.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> accountNos = accounts.stream().map(AccountInfo::getAccountNo).collect(Collectors.toList());
        return accountBalanceRepository.findByAccountNos(accountNos);
    }

    public AccountBalance getAccountBalance(String accountNo, String currencyCode) {
        return accountBalanceRepository.findByAccountAndCurrency(accountNo, currencyCode);
    }

    public String findAccountNoByUserAndCurrency(String userNo, String currencyCode) {
        List<AccountInfo> userAccounts = accountInfoRepository.findByUserNo(userNo);
        for (AccountInfo info : userAccounts) {
            AccountBalance balance = accountBalanceRepository.findByAccountAndCurrency(info.getAccountNo(),
                    currencyCode);
            if (balance != null) {
                return info.getAccountNo();
            }
        }
        return null;
    }

    // ==================== 分户账查询 ====================

    public IPage<AccountSubLedger> pageSubLedgers(
            String userNo, String accountNo, String currencyCode, LocalDate startDate,
            LocalDate endDate, FlowDirection direction, BizType bizType,
            int page, int pageSize) {
        return accountSubLedgerRepository.page(userNo, accountNo, currencyCode, startDate, endDate, direction, bizType,
                page, pageSize);
    }

    // ==================== 资产视图构建 ====================

    public UserAssetView buildUserAssetView(List<AccountInfo> accounts, List<AccountBalance> balances,
            Map<String, BigDecimal> rateMap) {
        if (accounts == null || accounts.isEmpty()) {
            return UserAssetView.empty();
        }

        Map<String, List<AccountBalance>> balanceMap = balances.stream()
                .collect(Collectors.groupingBy(AccountBalance::getAccountNo));

        BigDecimal totalMopValue = calculateAssetSummary(balances, rateMap);

        return UserAssetView.builder()
                .totalMopValue(totalMopValue)
                .accounts(accounts)
                .balanceMap(balanceMap)
                .build();
    }

    public BigDecimal calculateAssetSummary(List<AccountBalance> balances, Map<String, BigDecimal> rateMap) {
        if (balances == null || balances.isEmpty() || rateMap == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalMopValue = BigDecimal.ZERO;
        for (AccountBalance balance : balances) {
            BigDecimal rate = rateMap.get(balance.getCurrencyCode());
            if (rate != null) {
                totalMopValue = totalMopValue.add(
                        balance.getBalance().multiply(rate).setScale(2, RoundingMode.HALF_UP));
            }
        }
        return totalMopValue;
    }

    // ==================== 账户创建 ====================

    @Transactional(rollbackFor = Exception.class)
    public String createAccount(String userNo, AccountType accountType, Money initialBalance) {
        String currencyCode = initialBalance.getCurrencyCode();
        AccountCurrencyConfig currencyConfig = accountCurrencyConfigRepository.findByCurrencyCode(currencyCode);
        if (currencyConfig == null || CommonStatus.DISABLED == currencyConfig.getStatus()) {
            throw new BusinessException(AccountErrorCode.CURRENCY_NOT_SUPPORTED);
        }

        AccountInfo existingAccountInfo = null;
        List<AccountInfo> existingAccountInfos = getAccountInfosByUserNo(userNo);
        if (existingAccountInfos != null && !existingAccountInfos.isEmpty()) {
            existingAccountInfo = existingAccountInfos.get(0);
        }

        String accountNo;
        if (existingAccountInfo == null) {
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setUserNo(userNo);
            accountNo = accountSequenceGenerator.nextAccountNo();
            accountInfo.setAccountNo(accountNo);
            accountInfo.setCardNumber(cardNumberGenerator.nextCardNumber());
            accountInfo.setAccountType(accountType);
            accountInfo.setStatus(AccountStatus.NORMAL);
            accountInfo.setCreateTime(LocalDateTime.now());
            accountInfo.setUpdateTime(LocalDateTime.now());
            accountInfoRepository.save(accountInfo);
        } else {
            accountNo = existingAccountInfo.getAccountNo();
        }

        AccountBalance existingBalance = accountBalanceRepository.findByAccountAndCurrency(accountNo, currencyCode);
        if (existingBalance != null) {
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }

        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAccountNo(accountNo);
        accountBalance.setCurrencyCode(currencyCode);
        BigDecimal amount = initialBalance.getAmount();
        accountBalance.setBalance(amount);
        accountBalance.setAvailableBalance(amount);
        accountBalance.setFrozenAmount(BigDecimal.ZERO);
        accountBalance.setTotalIncome(amount);
        accountBalance.setTotalOutcome(BigDecimal.ZERO);

        accountBalanceRepository.save(accountBalance);

        return accountNo;
    }

    /**
     * 开通币种账户
     */
    public void openCurrencyAccount(String accountNo, String currencyCode) {
        AccountInfo accountInfo = accountInfoRepository.findByAccountNo(accountNo);
        if (accountInfo == null) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }

        AccountCurrencyConfig currencyConfig = accountCurrencyConfigRepository.findByCurrencyCode(currencyCode);
        if (currencyConfig == null || CommonStatus.DISABLED == currencyConfig.getStatus()) {
            throw new BusinessException(AccountErrorCode.CURRENCY_NOT_SUPPORTED);
        }

        AccountBalance existing = accountBalanceRepository.findByAccountAndCurrency(accountNo, currencyCode);
        if (existing != null) {
            return;
        }

        AccountBalance newBalance = new AccountBalance();
        newBalance.setAccountNo(accountNo);
        newBalance.setCurrencyCode(currencyCode);
        newBalance.setBalance(BigDecimal.ZERO);
        newBalance.setAvailableBalance(BigDecimal.ZERO);
        newBalance.setFrozenAmount(BigDecimal.ZERO);
        newBalance.setTotalIncome(BigDecimal.ZERO);
        newBalance.setTotalOutcome(BigDecimal.ZERO);

        accountBalanceRepository.save(newBalance);
    }
}
