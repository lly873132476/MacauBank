package com.macau.bank.account.integration;

import com.macau.bank.account.domain.entity.AccountBalance;
import com.macau.bank.account.domain.model.BalanceAdjustment;
import com.macau.bank.account.domain.repository.AccountBalanceRepository;
import com.macau.bank.account.domain.service.AccountBalanceDomainService;
import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 账户余额真正的集成测试
 * <p>
 * 测试目标：验证调用 Service 后，数据库中的余额【真的变了】
 * <p>
 * 技术栈：
 * - SpringBootTest 启动完整容器
 * - H2 内存数据库（不依赖外部 MySQL）
 * - @Transactional 回滚保证测试隔离
 * <p>
 * 致敬 ACTS：用数据驱动覆盖边界场景
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional // 每个测试执行完自动回滚，保证隔离
@DisplayName("账户余额集成测试 - 验证数据库真实变化")
class AccountBalanceIntegrationTest {

    @Autowired
    private AccountBalanceDomainService balanceService;

    @Autowired
    private AccountBalanceRepository balanceRepository;

    @DisplayName("扣款场景 - 验证数据库余额真实减少")
    @ParameterizedTest(name = "[{index}] {0}: 扣{1}元, 预期余额={2}")
    @CsvSource({
            // 场景描述, 扣款金额, 预期余额 (初始余额 10000)
            "正常扣款-500元,   500.00,    9500.00",
            "正常扣款-1000元,  1000.00,   9000.00",
            "清零扣款,         10000.00,  0.00"
    })
    void test_debit_real_balance_change(String scenario,
            BigDecimal debitAmount,
            BigDecimal expectedBalance) {
        // Given - 从数据库查出真实余额
        AccountBalance before = balanceRepository.findByAccountAndCurrency("ACC_001", "MOP");
        assertEquals(new BigDecimal("10000.00"), before.getBalance(), "初始余额应该是 10000");

        // When - 调用 Service 执行扣款（负数=出账）
        BalanceAdjustment adjustment = BalanceAdjustment.builder()
                .accountNo("ACC_001")
                .amount(Money.of(debitAmount.negate(), "MOP")) // 负数表示出账
                .bizType(BizType.TRANSFER_OUT)
                .bizNo("TXN_TEST_" + System.currentTimeMillis())
                .requestId("REQ_" + System.currentTimeMillis())
                .description("测试扣款")
                .build();
        balanceService.adjustBalance(adjustment);

        // Then - 【关键】再次从数据库查询，验证余额真的变了
        AccountBalance after = balanceRepository.findByAccountAndCurrency("ACC_001", "MOP");
        assertEquals(0, expectedBalance.compareTo(after.getBalance()),
                "场景 [" + scenario + "] 数据库余额应该变成 " + expectedBalance);
    }

    @DisplayName("入账场景 - 验证数据库余额真实增加")
    @ParameterizedTest(name = "[{index}] {0}: 入{1}元, 预期余额={2}")
    @CsvSource({
            // 场景描述, 入账金额, 预期余额 (初始余额 10000)
            "正常入账-500元,   500.00,    10500.00",
            "正常入账-1000元,  1000.00,   11000.00"
    })
    void test_credit_real_balance_change(String scenario,
            BigDecimal creditAmount,
            BigDecimal expectedBalance) {
        // Given
        AccountBalance before = balanceRepository.findByAccountAndCurrency("ACC_001", "MOP");
        assertEquals(new BigDecimal("10000.00"), before.getBalance());

        // When - 正数表示入账
        BalanceAdjustment adjustment = BalanceAdjustment.builder()
                .accountNo("ACC_001")
                .amount(Money.of(creditAmount, "MOP"))
                .bizType(BizType.TRANSFER_IN)
                .bizNo("TXN_TEST_" + System.currentTimeMillis())
                .requestId("REQ_" + System.currentTimeMillis())
                .description("测试入账")
                .build();
        balanceService.adjustBalance(adjustment);

        // Then - 验证数据库余额真的增加了
        AccountBalance after = balanceRepository.findByAccountAndCurrency("ACC_001", "MOP");
        assertEquals(0, expectedBalance.compareTo(after.getBalance()),
                "场景 [" + scenario + "] 数据库余额应该变成 " + expectedBalance);
    }

    @Test
    @DisplayName("余额不足场景 - 数据库余额不应改变")
    void test_insufficient_balance_should_not_change() {
        // Given
        AccountBalance before = balanceRepository.findByAccountAndCurrency("ACC_001", "MOP");
        BigDecimal originalBalance = before.getBalance();

        // When & Then - 扣 20000，余额只有 10000，应该抛异常
        BalanceAdjustment adjustment = BalanceAdjustment.builder()
                .accountNo("ACC_001")
                .amount(Money.of(new BigDecimal("-20000.00"), "MOP"))
                .bizType(BizType.TRANSFER_OUT)
                .bizNo("TXN_FAIL")
                .requestId("REQ_FAIL")
                .description("测试余额不足")
                .build();

        assertThrows(BusinessException.class, () -> balanceService.adjustBalance(adjustment));

        // 关键：余额不应该变
        AccountBalance after = balanceRepository.findByAccountAndCurrency("ACC_001", "MOP");
        assertEquals(0, originalBalance.compareTo(after.getBalance()),
                "余额不足时，数据库余额不应该改变");
    }

    @Test
    @DisplayName("冻结场景 - 验证可用余额减少、冻结金额增加")
    void test_freeze_real_balance_change() {
        // Given
        AccountBalance before = balanceRepository.findByAccountAndCurrency("ACC_001", "MOP");
        assertEquals(new BigDecimal("10000.00"), before.getAvailableBalance());
        assertEquals(new BigDecimal("0.00"), before.getFrozenAmount());

        // When
        Money freezeAmount = Money.of(new BigDecimal("3000.00"), "MOP");
        balanceService.freezeBalance("ACC_001", freezeAmount, "FREEZE_001",
                com.macau.bank.common.core.enums.FreezeType.TRANSACTION, "转账冻结");

        // Then - 验证数据库冻结金额真的变了
        AccountBalance after = balanceRepository.findByAccountAndCurrency("ACC_001", "MOP");
        assertEquals(0, new BigDecimal("7000.00").compareTo(after.getAvailableBalance()),
                "冻结后可用余额应该减少到 7000");
        assertEquals(0, new BigDecimal("3000.00").compareTo(after.getFrozenAmount()),
                "冻结金额应该增加到 3000");
        assertEquals(0, new BigDecimal("10000.00").compareTo(after.getBalance()),
                "总余额不应该变");
    }
}
