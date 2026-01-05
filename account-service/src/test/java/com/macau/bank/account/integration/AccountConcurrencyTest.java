package com.macau.bank.account.integration;

import com.macau.bank.account.common.result.AccountErrorCode;
import com.macau.bank.account.domain.entity.AccountBalance;
import com.macau.bank.account.domain.entity.AccountInfo;
import com.macau.bank.account.domain.repository.AccountBalanceRepository;
import com.macau.bank.account.domain.repository.AccountInfoRepository;
import com.macau.bank.common.core.enums.AccountCategory;
import com.macau.bank.common.core.enums.AccountStatus;
import com.macau.bank.common.core.enums.AccountType;
import com.macau.bank.common.core.exception.BusinessException;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 账户并发/乐观锁集成测试
 * <p>
 * 目标:验证 Repository 层准确处理乐观锁冲突
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration",
        "rocketmq.name-server="
})
@DisplayName("乐观锁机制验证测试")
class AccountConcurrencyTest {

    @Autowired
    private AccountBalanceRepository balanceRepository;

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @MockBean
    private XxlJobSpringExecutor xxlJobSpringExecutor;

    private static final String TEST_ACCOUNT_NO = "ACC_LOCK_TEST_001";
    private static final String TEST_CURRENCY = "MOP";

    @BeforeEach
    void setUp() {
        // 幂等性检查:如果账户已存在则跳过初始化
        AccountInfo existing = accountInfoRepository.findByAccountNo(TEST_ACCOUNT_NO);
        if (existing != null) {
            return; // 已经初始化过了
        }

        // 1. 准备账户基础信息
        AccountInfo info = new AccountInfo();
        info.setUserNo("USER_LOCK_TEST");
        info.setAccountNo(TEST_ACCOUNT_NO);
        info.setCardNumber("6228008888");
        info.setAccountCategory(AccountCategory.PERSONAL);
        info.setAccountType(AccountType.SAVINGS);
        info.setStatus(AccountStatus.NORMAL);
        accountInfoRepository.save(info);

        // 2. 准备初始余额 (Version = 0)
        AccountBalance balance = new AccountBalance();
        balance.setAccountNo(TEST_ACCOUNT_NO);
        balance.setCurrencyCode(TEST_CURRENCY);
        balance.setBalance(new BigDecimal("1000.00"));
        balance.setAvailableBalance(new BigDecimal("1000.00"));
        balance.setFrozenAmount(BigDecimal.ZERO);
        balance.setTotalIncome(new BigDecimal("1000.00"));
        balance.setTotalOutcome(BigDecimal.ZERO);
        balance.setId(null); // 让 Repository 处理 insert
        balanceRepository.save(balance);
    }

    @AfterEach
    void tearDown() {
        // H2 内存库自动重置，不需要 delete
    }

    /**
     * 确定性测试：模拟两个线程/进程持有同一个旧版本对象，先后提交
     */
    @Test
    @DisplayName("确定性乐观锁冲突验证")
    void testOptimisticLocking_Deterministic() {
        // 1. 模拟线程 A 读取数据 (Version = 0)
        AccountBalance viewA = balanceRepository.findByAccountAndCurrency(TEST_ACCOUNT_NO, TEST_CURRENCY);
        assertNotNull(viewA);
        assertEquals(0, viewA.getVersion(), "初始版本应该是 0");

        // 2. 模拟线程 B 读取同一份数据 (Version = 0)
        // 这里为了模拟真实对象隔离，我们重新查一次，或者直接 clone viewA
        // 在 JPA/MyBatis 中，find 出来的是不同对象引用，但内容一致
        AccountBalance viewB = balanceRepository.findByAccountAndCurrency(TEST_ACCOUNT_NO, TEST_CURRENCY);
        assertEquals(0, viewB.getVersion(), "线程B读到的版本也应该是 0");

        // 3. 线程 A 修改并提交 -> 应该成功
        viewA.setBalance(viewA.getBalance().subtract(new BigDecimal("100.00")));
        viewA.setAvailableBalance(viewA.getAvailableBalance().subtract(new BigDecimal("100.00")));
        assertDoesNotThrow(() -> balanceRepository.save(viewA), "线程A提交应该成功");

        // 检查数据库，版本应该变成 1
        AccountBalance currentInDb = balanceRepository.findByAccountAndCurrency(TEST_ACCOUNT_NO, TEST_CURRENCY);
        assertEquals(1, currentInDb.getVersion(), "数据库版本号应该增加到 1");

        // 4. 线程 B (持有旧的 Version 0) 修改并提交 -> 应该失败
        // viewB 的 version 仍然是 0
        assertEquals(0, viewB.getVersion());
        viewB.setBalance(viewB.getBalance().subtract(new BigDecimal("50.00")));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            balanceRepository.save(viewB);
        }, "使用旧版本提交应该抛出 BusinessException");

        // 验证异常码
        assertEquals(AccountErrorCode.CONCURRENCY_CONFLICT.getCode(), exception.getCode(), "异常码应该是并发冲突");

        // 5. 验证数据库数据没有被 B 覆盖
        AccountBalance finalState = balanceRepository.findByAccountAndCurrency(TEST_ACCOUNT_NO, TEST_CURRENCY);
        assertEquals(new BigDecimal("900.00"), finalState.getBalance(), "余额应该只被A扣减了100");
    }
}
