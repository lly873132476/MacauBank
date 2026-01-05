package com.macau.bank.account.domain.entity;

import com.macau.bank.common.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 账户余额变动核心测试
 * <p>
 * 致敬 ACTS 的数据驱动思想：
 * 通过参数化测试，用极少的代码行数，覆盖 余额不足、正好扣光、正常扣款 等多种业务场景。
 * <p>
 * 亮点：
 * 1. 一个测试方法，5 行数据，跑出 5 个 Test Case
 * 2. 覆盖了"刚好够"这种最容易出 Bug 的临界点
 * 3. 不需要 Spring，直接测 Entity 的业务逻辑
 */
@DisplayName("账户余额参数化测试 (ACTS 风格)")
class AccountBalanceParameterizedTest {

    @DisplayName("扣款场景边界测试")
    @ParameterizedTest(name = "[{index}] {0}: 初始={1}, 扣减={2}, 预期={3}, 抛异常={4}")
    @CsvSource({
            // 场景描述, 初始余额, 扣减金额, 预期余额, 是否预期抛异常
            "余额充足-正常扣款,   100.00,    50.00,    50.00,    false",
            "余额刚好-清零测试,   100.00,    100.00,   0.00,     false",
            "余额不足-差一分钱,   100.00,    100.01,   100.00,   true",
            "扣减负数-非法参数,   100.00,    -10.00,   100.00,   true",
            "余额为零-扣款失败,   0.00,      10.00,    0.00,     true"
    })
    void test_debit_scenarios(String scenario,
            BigDecimal initBalance,
            BigDecimal debitAmount,
            BigDecimal expectedBalance,
            boolean expectException) {

        // 1. 准备数据 (Given)
        AccountBalance balance = new AccountBalance();
        balance.setAvailableBalance(initBalance);
        balance.setBalance(initBalance);

        // 2. 执行与验证 (When & Then)
        if (expectException) {
            // 预期报错的场景
            assertThrows(BusinessException.class,
                    () -> balance.checkAndDebit(debitAmount),
                    "场景 [" + scenario + "] 应该抛出异常但没抛");
        } else {
            // 预期成功的场景
            assertDoesNotThrow(() -> balance.checkAndDebit(debitAmount));
            // 验证余额扣对了没
            assertEquals(0, expectedBalance.compareTo(balance.getAvailableBalance()),
                    "场景 [" + scenario + "] 余额计算错误");
        }
    }

    @DisplayName("冻结场景边界测试")
    @ParameterizedTest(name = "[{index}] {0}: 可用={1}, 冻结={2}, 预期可用={3}, 预期冻结={4}, 抛异常={5}")
    @CsvSource({
            // 场景描述, 可用余额, 冻结金额, 预期可用, 预期冻结, 是否预期抛异常
            "正常冻结-部分金额,     1000.00,   300.00,   700.00,    300.00,    false",
            "正常冻结-全部金额,     1000.00,   1000.00,  0.00,      1000.00,   false",
            "冻结失败-余额不足,     1000.00,   1000.01,  1000.00,   0.00,      true",
            "冻结失败-负数金额,     1000.00,   -100.00,  1000.00,   0.00,      true"
    })
    void test_freeze_scenarios(String scenario,
            BigDecimal availableBalance,
            BigDecimal freezeAmount,
            BigDecimal expectedAvailable,
            BigDecimal expectedFrozen,
            boolean expectException) {

        // 1. 准备数据 (Given)
        AccountBalance balance = new AccountBalance();
        balance.setAvailableBalance(availableBalance);
        balance.setBalance(availableBalance);
        balance.setFrozenAmount(BigDecimal.ZERO);

        // 2. 执行与验证 (When & Then)
        if (expectException) {
            assertThrows(BusinessException.class,
                    () -> balance.checkAndFreeze(freezeAmount),
                    "场景 [" + scenario + "] 应该抛出异常但没抛");
        } else {
            assertDoesNotThrow(() -> balance.checkAndFreeze(freezeAmount));
            assertEquals(0, expectedAvailable.compareTo(balance.getAvailableBalance()),
                    "场景 [" + scenario + "] 可用余额计算错误");
            assertEquals(0, expectedFrozen.compareTo(balance.getFrozenAmount()),
                    "场景 [" + scenario + "] 冻结金额计算错误");
        }
    }
}
