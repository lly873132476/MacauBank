package com.macau.bank.account.domain.entity;

import com.macau.bank.common.core.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户余额领域实体
 */
@Getter
@Setter
@ToString
public class AccountBalance implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String accountNo;
    private String currencyCode;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalOutcome;
    private Integer version;
    private String lastFlowId;
    private String macCode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 余额校验
     * <p>
     * 业务规则：
     * 1. 判断余额是否足够扣款
     *
     * @param amount 扣款金额
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return availableBalance != null && availableBalance.compareTo(amount) >= 0;
    }

    /**
     * 检查并扣款（核心业务方法）
     * <p>
     * 业务规则：
     * 1. 扣款金额必须为正数
     * 2. 可用余额必须充足
     * 
     * @param amount 扣款金额（必须为正数）
     * @throws BusinessException 余额不足或金额非法时抛出
     */
    public void checkAndDebit(BigDecimal amount) {
        // 1. 参数校验：金额必须为正数
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "INVALID_AMOUNT", "扣款金额必须为正数");
        }

        // 2. 余额校验
        if (!hasSufficientBalance(amount)) {
            throw new BusinessException(
                    "BALANCE_INSUFFICIENT", "可用余额不足");
        }

        // 3. 执行扣款
        this.availableBalance = this.availableBalance.subtract(amount);
        this.balance = this.balance.subtract(amount);
        this.totalOutcome = (this.totalOutcome == null ? BigDecimal.ZERO : this.totalOutcome).add(amount);
    }

    /**
     * 检查并冻结（核心业务方法）
     * <p>
     * 业务规则：
     * 1. 冻结金额必须为正数
     * 2. 可用余额必须充足
     * 
     * @param amount 冻结金额（必须为正数）
     * @throws BusinessException 余额不足或金额非法时抛出
     */
    public void checkAndFreeze(BigDecimal amount) {
        // 1. 参数校验：金额必须为正数
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "INVALID_AMOUNT", "冻结金额必须为正数");
        }

        // 2. 余额校验
        if (!hasSufficientBalance(amount)) {
            throw new BusinessException(
                    "BALANCE_INSUFFICIENT", "可用余额不足");
        }

        // 3. 执行冻结（可用余额减少，冻结金额增加，总余额不变）
        this.availableBalance = this.availableBalance.subtract(amount);
        this.frozenAmount = (this.frozenAmount == null ? BigDecimal.ZERO : this.frozenAmount).add(amount);
    }
}