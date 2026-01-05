package com.macau.bank.account.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.macau.bank.account.common.result.AccountErrorCode;
import com.macau.bank.account.domain.entity.*;
import com.macau.bank.account.domain.model.BalanceAdjustment;
import com.macau.bank.account.domain.model.UserAssetView;
import com.macau.bank.account.domain.repository.*;
import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.*;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.exception.FatalSystemException;
import com.macau.bank.common.core.util.IdGenerator;
import com.macau.bank.common.sequence.AccountSequenceGenerator;
import com.macau.bank.common.sequence.CardNumberGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
 * 负责账户核心业务逻辑处理 and 数据持久化
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
    private AccountFreezeLogRepository accountFreezeLogRepository;

    @Resource
    private AccountSequenceGenerator accountSequenceGenerator;

    @Resource
    private CardNumberGenerator cardNumberGenerator;

    /**
     * 检查幂等请求ID是否已存在
     */
    public AccountSubLedger findByRequestId(String requestId) {
        if (!StringUtils.hasText(requestId)) {
            return null;
        }
        return accountSubLedgerRepository.findByRequestId(requestId);
    }

    private boolean checkIdempotent(String requestId, Money amount, String operation) {
        if (!StringUtils.hasText(requestId)) {
            log.error("{} 缺少幂等ID", operation);
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }
        AccountSubLedger existing = findByRequestId(requestId);
        if (existing != null) {
            boolean amountMatch = existing.getAmount().compareTo(amount.getAmount()) == 0;
            boolean currencyMatch = existing.getCurrencyCode().equals(amount.getCurrencyCode());

            if (!amountMatch || !currencyMatch) {
                throw new FatalSystemException(
                        String.format("资损警报：幂等攻击拦截！RequestID=%s, DB=[%s %s], 新请求=[%s]",
                                requestId, existing.getCurrencyCode(), existing.getAmount(), amount.display()));
            }
            log.warn("重复请求被拦截: requestId={}, operation={}", requestId, operation);
            return true;
        }
        return false;
    }

    public IPage<AccountSubLedger> pageSubLedgers(
            String userNo, String accountNo, String currencyCode, LocalDate startDate,
            LocalDate endDate, FlowDirection direction, BizType bizType,
            int page, int pageSize) {

        return accountSubLedgerRepository.page(userNo, accountNo, currencyCode, startDate, endDate, direction, bizType,
                page, pageSize);
    }

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

    public List<AccountInfo> getAccountInfosByUserNo(String userNo) {
        return accountInfoRepository.findByUserNo(userNo);
    }

    public AccountInfo getAccountInfoByAccountNo(String accountNo) {
        return accountInfoRepository.findByAccountNo(accountNo);
    }

    public AccountInfo getAccountInfoByCardNumber(String cardNumber) {
        return accountInfoRepository.findByCardNumber(cardNumber);
    }

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
        // 复杂关联查询暂时简化，或在 Repository 增加自定义 join
        // 鉴于时间，保留逻辑但通过应用层拼装 (性能稍差但符合分层)
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

    @Transactional
    public boolean adjustBalance(BalanceAdjustment adjustment) {
        adjustment.validate();
        String accountNo = adjustment.getAccountNo();
        Money amount = adjustment.getAmount();

        AccountInfo accountInfo = getAccountInfoByAccountNo(accountNo);
        if (accountInfo == null) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }

        if (amount.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }

        if (checkIdempotent(adjustment.getRequestId(), amount.abs(), "adjustBalance")) {
            return true;
        }

        AccountBalance currentBalance = accountBalanceRepository.findByAccountAndCurrency(accountNo,
                amount.getCurrencyCode());
        if (currentBalance == null) {
            throw new BusinessException(AccountErrorCode.BALANCE_RECORD_NOT_FOUND);
        }

        BigDecimal changeAmount = amount.getAmount();
        if (amount.isNegative()) {
            if (!currentBalance.hasSufficientBalance(changeAmount.abs())) {
                throw new BusinessException(AccountErrorCode.BALANCE_INSUFFICIENT);
            }
        }

        String voucherNo = IdGenerator.generateId();

        currentBalance.setBalance(currentBalance.getBalance().add(changeAmount));
        currentBalance.setAvailableBalance(currentBalance.getAvailableBalance().add(changeAmount));
        if (amount.isPositive()) {
            currentBalance.setTotalIncome(currentBalance.getTotalIncome().add(changeAmount));
        } else {
            currentBalance.setTotalOutcome(currentBalance.getTotalOutcome().add(changeAmount.abs()));
        }
        currentBalance.setLastFlowId(voucherNo);

        accountBalanceRepository.save(currentBalance);

        recordSubLedger(accountInfo, currentBalance, amount, adjustment.getDescription(), adjustment.getBizNo(),
                adjustment.getRequestId(), voucherNo, adjustment.getBizType());
        return true;
    }

    private void recordSubLedger(AccountInfo accountInfo, AccountBalance balance, Money amount, String desc,
            String bizNo, String requestId, String voucherNo, BizType bizType) {
        AccountSubLedger subLedger = new AccountSubLedger();
        subLedger.setVoucherNo(voucherNo);
        subLedger.setBiz_no(bizNo);
        subLedger.setRequestId(requestId);
        subLedger.setUserNo(accountInfo.getUserNo());
        subLedger.setAccountNo(accountInfo.getAccountNo());
        subLedger.setCurrencyCode(balance.getCurrencyCode());

        FlowDirection direction = FlowDirection.resolve(amount.getAmount());
        subLedger.setCdFlag(direction);
        subLedger.setAmount(amount.abs().getAmount());
        subLedger.setBalance(balance.getBalance());

        subLedger.setStatus(AccountingStatus.NORMAL);
        subLedger.setCheckStatus(CheckStatus.UNCHECKED);
        subLedger.setSettleStatus(SettleStatus.REALTIME);
        subLedger.setBizType(bizType);
        subLedger.setBizDesc(desc);

        subLedger.setAcctDate(LocalDate.now());
        subLedger.setTransTime(LocalDateTime.now());

        accountSubLedgerRepository.save(subLedger);
    }

    @Transactional
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

    @Transactional
    public boolean freezeBalance(String accountNo, Money amount, String flowNo, FreezeType freezeType, String reason) {
        if (!amount.isPositive())
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);

        AccountBalance current = accountBalanceRepository.findByAccountAndCurrency(accountNo, amount.getCurrencyCode());
        if (current == null)
            throw new BusinessException(AccountErrorCode.BALANCE_RECORD_NOT_FOUND);

        if (current.getAvailableBalance().compareTo(amount.getAmount()) < 0) {
            throw new BusinessException(AccountErrorCode.BALANCE_INSUFFICIENT);
        }

        current.setAvailableBalance(current.getAvailableBalance().subtract(amount.getAmount()));
        current.setFrozenAmount(current.getFrozenAmount().add(amount.getAmount()));

        accountBalanceRepository.save(current);

        AccountFreezeLog freezeLog = new AccountFreezeLog();
        freezeLog.setFlowNo(flowNo != null ? flowNo : IdGenerator.generateId());
        freezeLog.setAccountNo(accountNo);
        freezeLog.setCurrencyCode(amount.getCurrencyCode());
        freezeLog.setAmount(amount.getAmount());
        freezeLog.setFreezeType(freezeType);
        freezeLog.setReason(reason);
        freezeLog.setStatus(FreezeStatus.FROZEN);
        freezeLog.setCreateTime(LocalDateTime.now());
        accountFreezeLogRepository.save(freezeLog);

        return true;
    }

    @Transactional
    public boolean unfreezeBalance(String accountNo, Money amount, String flowNo, String reason) {
        if (!amount.isPositive())
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);

        AccountBalance current = accountBalanceRepository.findByAccountAndCurrency(accountNo, amount.getCurrencyCode());
        if (current == null)
            throw new BusinessException(AccountErrorCode.BALANCE_RECORD_NOT_FOUND);

        if (current.getFrozenAmount().compareTo(amount.getAmount()) < 0) {
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }

        current.setAvailableBalance(current.getAvailableBalance().add(amount.getAmount()));
        current.setFrozenAmount(current.getFrozenAmount().subtract(amount.getAmount()));

        accountBalanceRepository.save(current);

        AccountFreezeLog freezeLog = accountFreezeLogRepository.findByFlowNo(flowNo);
        if (freezeLog != null) {
            freezeLog.setStatus(FreezeStatus.UNFROZEN);
            freezeLog.setUnfreezeTime(LocalDateTime.now());
            freezeLog.setReason(freezeLog.getReason() + " (解冻原因: " + reason + ")");
            accountFreezeLogRepository.save(freezeLog);
        }
        return true;
    }

    /**
     * 解冻并扣款（原子操作）
     * <p>
     * 场景：转账冻结 → 确认成功 → 解冻并扣款
     * 操作：从冻结金额中减少，同时从总余额中减少（可用余额不变）
     *
     * @param accountNo 账户号
     * @param amount    金额（正数）
     * @param flowNo    冻结流水号
     * @param reason    扣款原因
     * @param bizType   业务类型（由上游传递）
     * @param requestId 请求唯一标识（用于幂等性校验）
     * @return 是否成功
     */
    @Transactional
    public boolean unfreezeAndDebit(String accountNo, Money amount, String flowNo, String reason,
            BizType bizType, String requestId) {
        if (!amount.isPositive()) {
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }

        // 0. 幂等性校验
        AccountSubLedger existing = findByRequestId(requestId);
        if (existing != null) {
            log.info("解冻扣款重复请求，直接返回成功: requestId={}", requestId);
            return true;
        }

        // 1. 查询账户信息
        AccountInfo accountInfo = accountInfoRepository.findByAccountNo(accountNo);
        if (accountInfo == null) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }

        // 2. 查询余额记录
        AccountBalance current = accountBalanceRepository.findByAccountAndCurrency(accountNo, amount.getCurrencyCode());
        if (current == null) {
            throw new BusinessException(AccountErrorCode.BALANCE_RECORD_NOT_FOUND);
        }

        // 3. 校验冻结金额是否足够
        if (current.getFrozenAmount().compareTo(amount.getAmount()) < 0) {
            throw new BusinessException(AccountErrorCode.FROZEN_BALANCE_NOT_ENOUGH);
        }

        // 4. 生成凭证号
        String voucherNo = IdGenerator.generateId();

        // 5. 执行解冻并扣款：冻结金额减少，总余额减少，可用余额不变
        current.setFrozenAmount(current.getFrozenAmount().subtract(amount.getAmount()));
        current.setBalance(current.getBalance().subtract(amount.getAmount()));
        current.setTotalOutcome(current.getTotalOutcome().add(amount.getAmount()));
        current.setLastFlowId(voucherNo);
        accountBalanceRepository.save(current);

        // 6. 更新冻结日志
        AccountFreezeLog freezeLog = accountFreezeLogRepository.findByFlowNo(flowNo);
        if (freezeLog != null) {
            freezeLog.setStatus(FreezeStatus.DEDUCTED);
            freezeLog.setUnfreezeTime(LocalDateTime.now());
            freezeLog.setReason(freezeLog.getReason() + " (扣款原因: " + reason + ")");
            accountFreezeLogRepository.save(freezeLog);
        }

        // 7. 记录分户账（出账为负数）
        recordSubLedger(accountInfo, current, amount.negate(), reason, flowNo, requestId, voucherNo, bizType);

        return true;
    }

    public void openCurrencyAccount(String accountNo, String currencyCode) {
        AccountInfo accountInfo = accountInfoRepository.findByAccountNo(accountNo);
        if (accountInfo == null)
            throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);

        AccountCurrencyConfig currencyConfig = accountCurrencyConfigRepository.findByCurrencyCode(currencyCode);
        if (currencyConfig == null || CommonStatus.DISABLED == currencyConfig.getStatus()) {
            throw new BusinessException(AccountErrorCode.CURRENCY_NOT_SUPPORTED);
        }

        AccountBalance existing = accountBalanceRepository.findByAccountAndCurrency(accountNo, currencyCode);
        if (existing != null)
            return;

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
