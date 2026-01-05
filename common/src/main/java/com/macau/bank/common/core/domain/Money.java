package com.macau.bank.common.core.domain;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 资金值对象 (Value Object)
 * 特性：不可变、线程安全、自带币种检查
 */
@Getter
@ToString
public class Money implements Serializable, Comparable<Money> {

    private static final long serialVersionUID = 1L;

    // 核心属性：金额（必须用 BigDecimal）
    private final BigDecimal amount;

    // 核心属性：币种（使用 ISO 4217 代码，如 MOP, HKD, CNY）
    private final String currencyCode;

    // 默认保留小数位
    private static final int DEFAULT_SCALE = 2;

    // 默认舍入模式：银行家舍入法 (四舍六入五成双) 或者 普通四舍五入
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    // ================= 构造工厂 =================

    /**
     * 私有构造，强制走静态工厂方法
     */
    private Money(BigDecimal amount, String currencyCode) {
        if (amount == null || currencyCode == null) {
            throw new IllegalArgumentException("Amount and Currency cannot be null");
        }
        this.amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
        this.currencyCode = currencyCode;
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyCode);
    }

    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), currencyCode);
    }

    public static Money of(String amountStr, String currencyCode) {
        return new Money(new BigDecimal(amountStr), currencyCode);
    }

    // 快捷创建零钱
    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, currencyCode);
    }

    // ================= 核心运算 (核心防御逻辑) =================

    /**
     * 加法
     */
    public Money add(Money other) {
        checkCurrency(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    /**
     * 减法
     */
    public Money subtract(Money other) {
        checkCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currencyCode);
    }

    /**
     * 乘法 (例如计算手续费 rate)
     */
    public Money multiply(double multiplier) {
        BigDecimal result = this.amount.multiply(BigDecimal.valueOf(multiplier));
        return new Money(result, this.currencyCode);
    }

    /**
     * 乘法 (例如 数量 * 单价)
     */
    public Money multiply(int quantity) {
        BigDecimal result = this.amount.multiply(BigDecimal.valueOf(quantity));
        return new Money(result, this.currencyCode);
    }

    /**
     * 乘法 (例如 汇率计算)
     */
    public Money multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new IllegalArgumentException("Multiplier cannot be null");
        }
        BigDecimal result = this.amount.multiply(multiplier);
        return new Money(result, this.currencyCode);
    }

    /**
     * 取绝对值
     */
    public Money abs() {
        return new Money(this.amount.abs(), this.currencyCode);
    }

    /**
     * 取反（正变负，负变正）
     */
    public Money negate() {
        return new Money(this.amount.negate(), this.currencyCode);
    }

    // ================= 辅助方法 =================

    /**
     * 检查币种是否一致 (这是 Money 类存在的最大意义！)
     */
    private void checkCurrency(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            // 抛出运行时异常，防止 MOP 和 HKD 直接相加
            throw new IllegalArgumentException(
                    String.format("Currency mismatch: cannot compute %s with %s",
                            this.currencyCode, other.currencyCode));
        }
    }

    /**
     * 比较大小
     */
    @Override
    public int compareTo(Money other) {
        checkCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    /**
     * 是否金额为正
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 是否金额为负 (透支)
     */
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 是否相等 (值对象必须重写 equals)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) &&
                Objects.equals(currencyCode, money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }

    // 给前端显示的格式化字符串 (例如: MOP 100.00)
    public String display() {
        return this.currencyCode + " " + this.amount.toPlainString();
    }
}