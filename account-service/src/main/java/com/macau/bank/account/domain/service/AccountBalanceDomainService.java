package com.macau.bank.account.domain.service;

import com.macau.bank.account.common.result.AccountErrorCode;
import com.macau.bank.account.domain.entity.*;
import com.macau.bank.account.domain.model.BalanceAdjustment;
import com.macau.bank.account.domain.repository.*;
import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.*;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.exception.FatalSystemException;
import com.macau.bank.common.core.util.IdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账户余额领域服务
 * <p>
 * 职责：处理账户余额的核心业务逻辑
 * - 余额调整（入账/出账）
 * - 冻结/解冻
 * - 解冻并扣款
 * - 分户账记录
 */
@Slf4j
@Service
public class AccountBalanceDomainService {

    @Resource
    private AccountInfoRepository accountInfoRepository;

    @Resource
    private AccountBalanceRepository accountBalanceRepository;

    @Resource
    private AccountSubLedgerRepository accountSubLedgerRepository;

    @Resource
    private AccountFreezeLogRepository accountFreezeLogRepository;

    /**
     * 检查幂等请求ID是否已存在
     */
    public AccountSubLedger findByRequestId(String requestId) {
        if (!StringUtils.hasText(requestId)) {
            return null;
        }
        return accountSubLedgerRepository.findByRequestId(requestId);
    }

    /**
     * 幂等校验
     */
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

    /**
     * 余额调整（入账/出账）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustBalance(BalanceAdjustment adjustment) {
        adjustment.validate();
        String accountNo = adjustment.getAccountNo();
        Money amount = adjustment.getAmount();

        AccountInfo accountInfo = accountInfoRepository.findByAccountNo(accountNo);
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

    /**
     * 冻结余额
     * <p>
     * TCC Try 阶段 - 增加悬挂检测
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeBalance(String accountNo, Money amount, String flowNo, FreezeType freezeType, String reason) {
        if (!amount.isPositive()) {
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }

        // 1. TCC 悬挂检测: 检查是否已经 Cancel 过
        AccountFreezeLog existingLog = accountFreezeLogRepository.findByFlowNo(flowNo);
        if (existingLog != null && existingLog.getStatus() == FreezeStatus.CANCELLED) {
            log.warn("TCC 悬挂拦截: Cancel 已执行,拒绝 Try, flowNo={}, accountNo={}", flowNo, accountNo);
            return false;
        }

        // 2. 幂等性检查: 已经冻结过
        if (existingLog != null && existingLog.getStatus() == FreezeStatus.FROZEN) {
            log.warn("重复冻结请求,直接返回: flowNo={}, accountNo={}", flowNo, accountNo);
            return true;
        }

        AccountBalance current = accountBalanceRepository.findByAccountAndCurrency(accountNo, amount.getCurrencyCode());
        if (current == null) {
            throw new BusinessException(AccountErrorCode.BALANCE_RECORD_NOT_FOUND);
        }

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

    /**
     * 解冻余额
     * <p>
     * TCC Cancel 阶段 - 增加空回滚检测和幂等性保护
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeBalance(String accountNo, Money amount, String flowNo, String reason) {
        if (!amount.isPositive()) {
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }

        // 1. 查询冻结记录
        AccountFreezeLog freezeLog = accountFreezeLogRepository.findByFlowNo(flowNo);

        // 2. TCC 空回滚检测: Try 没执行过,插入 Cancel 标记
        if (freezeLog == null) {
            AccountFreezeLog cancelMarker = new AccountFreezeLog();
            cancelMarker.setFlowNo(flowNo);
            cancelMarker.setAccountNo(accountNo);
            cancelMarker.setCurrencyCode(amount.getCurrencyCode());
            cancelMarker.setAmount(amount.getAmount());
            cancelMarker.setFreezeType(FreezeType.TRANSACTION);
            cancelMarker.setStatus(FreezeStatus.CANCELLED);
            cancelMarker.setReason("TCC 空回滚标记: " + reason);
            cancelMarker.setCreateTime(LocalDateTime.now());
            accountFreezeLogRepository.save(cancelMarker);

            log.warn("TCC 空回滚拦截: Try 未执行,插入 Cancel 标记, flowNo={}, accountNo={}", flowNo, accountNo);
            return true;
        }

        // 3. 幂等性检查: 已经解冻过
        if (freezeLog.getStatus() == FreezeStatus.UNFROZEN) {
            log.warn("重复解冻请求,直接返回: flowNo={}, accountNo={}", flowNo, accountNo);
            return true;
        }

        // 4. 跳过空回滚标记 (不应该走到这里,但做防御性检查)
        if (freezeLog.getStatus() == FreezeStatus.CANCELLED) {
            log.warn("跳过空回滚标记: flowNo={}", flowNo);
            return true;
        }

        // 5. 正常解冻流程
        AccountBalance current = accountBalanceRepository.findByAccountAndCurrency(accountNo, amount.getCurrencyCode());
        if (current == null) {
            throw new BusinessException(AccountErrorCode.BALANCE_RECORD_NOT_FOUND);
        }

        if (current.getFrozenAmount().compareTo(amount.getAmount()) < 0) {
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }

        current.setAvailableBalance(current.getAvailableBalance().add(amount.getAmount()));
        current.setFrozenAmount(current.getFrozenAmount().subtract(amount.getAmount()));

        accountBalanceRepository.save(current);

        freezeLog.setStatus(FreezeStatus.UNFROZEN);
        freezeLog.setUnfreezeTime(LocalDateTime.now());
        freezeLog.setReason(freezeLog.getReason() + " (解冻原因: " + reason + ")");
        accountFreezeLogRepository.save(freezeLog);

        return true;
    }

    /**
     * 解冻并扣款（原子操作）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeAndDebit(String accountNo, Money amount, String flowNo, String reason,
            BizType bizType, String requestId) {
        if (!amount.isPositive()) {
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }

        // 幂等性校验
        AccountSubLedger existing = findByRequestId(requestId);
        if (existing != null) {
            log.info("解冻扣款重复请求，直接返回成功: requestId={}", requestId);
            return true;
        }

        // 查询账户信息
        AccountInfo accountInfo = accountInfoRepository.findByAccountNo(accountNo);
        if (accountInfo == null) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }

        // 查询余额记录
        AccountBalance current = accountBalanceRepository.findByAccountAndCurrency(accountNo, amount.getCurrencyCode());
        if (current == null) {
            throw new BusinessException(AccountErrorCode.BALANCE_RECORD_NOT_FOUND);
        }

        // 校验冻结金额是否足够
        if (current.getFrozenAmount().compareTo(amount.getAmount()) < 0) {
            throw new BusinessException(AccountErrorCode.FROZEN_BALANCE_NOT_ENOUGH);
        }

        // 生成凭证号
        String voucherNo = IdGenerator.generateId();

        // 执行解冻并扣款
        current.setFrozenAmount(current.getFrozenAmount().subtract(amount.getAmount()));
        current.setBalance(current.getBalance().subtract(amount.getAmount()));
        current.setTotalOutcome(current.getTotalOutcome().add(amount.getAmount()));
        current.setLastFlowId(voucherNo);
        accountBalanceRepository.save(current);

        // 更新冻结日志
        AccountFreezeLog freezeLog = accountFreezeLogRepository.findByFlowNo(flowNo);
        if (freezeLog != null) {
            freezeLog.setStatus(FreezeStatus.DEDUCTED);
            freezeLog.setUnfreezeTime(LocalDateTime.now());
            freezeLog.setReason(freezeLog.getReason() + " (扣款原因: " + reason + ")");
            accountFreezeLogRepository.save(freezeLog);
        }

        // 记录分户账（出账为负数）
        recordSubLedger(accountInfo, current, amount.negate(), reason, flowNo, requestId, voucherNo, bizType);

        return true;
    }

    /**
     * 记录分户账
     */
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
}
