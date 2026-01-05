package com.macau.bank.account.domain.service;

import com.macau.bank.account.common.result.AccountErrorCode;
import com.macau.bank.account.domain.entity.AccountBalance;
import com.macau.bank.account.domain.entity.AccountFreezeLog;
import com.macau.bank.account.domain.entity.AccountInfo;
import com.macau.bank.account.domain.entity.AccountSubLedger;
import com.macau.bank.account.domain.model.BalanceAdjustment;
import com.macau.bank.account.domain.repository.AccountBalanceRepository;
import com.macau.bank.account.domain.repository.AccountFreezeLogRepository;
import com.macau.bank.account.domain.repository.AccountInfoRepository;
import com.macau.bank.account.domain.repository.AccountSubLedgerRepository;
import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.FreezeType;
import com.macau.bank.common.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 账户余额领域服务测试
 * <p>
 * 测试策略：Mock Repository，验证 Service 方法的业务逻辑
 * <p>
 * 核心场景：
 * 1. 余额调整（入账/出账/余额不足）
 * 2. 冻结/解冻
 * 3. 解冻并扣款
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("账户余额领域服务测试")
class AccountBalanceDomainServiceTest {

    @Mock
    private AccountInfoRepository accountInfoRepository;

    @Mock
    private AccountBalanceRepository accountBalanceRepository;

    @Mock
    private AccountSubLedgerRepository accountSubLedgerRepository;

    @Mock
    private AccountFreezeLogRepository accountFreezeLogRepository;

    @InjectMocks
    private AccountBalanceDomainService service;

    private AccountInfo testAccountInfo;
    private AccountBalance testBalance;

    @BeforeEach
    void setUp() {
        testAccountInfo = new AccountInfo();
        testAccountInfo.setAccountNo("ACC_001");
        testAccountInfo.setUserNo("USER_001");

        testBalance = new AccountBalance();
        testBalance.setAccountNo("ACC_001");
        testBalance.setCurrencyCode("MOP");
        testBalance.setBalance(new BigDecimal("10000.00"));
        testBalance.setAvailableBalance(new BigDecimal("10000.00"));
        testBalance.setFrozenAmount(BigDecimal.ZERO);
        testBalance.setTotalIncome(BigDecimal.ZERO);
        testBalance.setTotalOutcome(BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("余额调整")
    class BalanceAdjustmentTests {

        @Test
        @DisplayName("入账应增加余额并记录分户账")
        void creditShouldIncreaseBalance() {
            // Given
            when(accountInfoRepository.findByAccountNo("ACC_001")).thenReturn(testAccountInfo);
            when(accountBalanceRepository.findByAccountAndCurrency("ACC_001", "MOP")).thenReturn(testBalance);
            when(accountSubLedgerRepository.findByRequestId(anyString())).thenReturn(null);

            // amount 正数 = 入账
            BalanceAdjustment adjustment = BalanceAdjustment.builder()
                    .accountNo("ACC_001")
                    .amount(Money.of(new BigDecimal("500.00"), "MOP"))
                    .bizType(BizType.TRANSFER_IN)
                    .bizNo("TXN_001")
                    .requestId("REQ_001")
                    .description("转账入账")
                    .build();

            // When
            boolean result = service.adjustBalance(adjustment);

            // Then
            assertTrue(result);
            assertEquals(new BigDecimal("10500.00"), testBalance.getBalance());
            assertEquals(new BigDecimal("10500.00"), testBalance.getAvailableBalance());
            verify(accountBalanceRepository).save(testBalance);
            verify(accountSubLedgerRepository).save(any(AccountSubLedger.class));
        }

        @Test
        @DisplayName("出账余额不足应抛出异常")
        void debitShouldThrowWhenInsufficientBalance() {
            // Given
            testBalance.setAvailableBalance(new BigDecimal("100.00"));
            when(accountInfoRepository.findByAccountNo("ACC_001")).thenReturn(testAccountInfo);
            when(accountBalanceRepository.findByAccountAndCurrency("ACC_001", "MOP")).thenReturn(testBalance);
            when(accountSubLedgerRepository.findByRequestId(anyString())).thenReturn(null);

            // amount 负数 = 出账
            BalanceAdjustment adjustment = BalanceAdjustment.builder()
                    .accountNo("ACC_001")
                    .amount(Money.of(new BigDecimal("-500.00"), "MOP"))
                    .bizType(BizType.TRANSFER_OUT)
                    .bizNo("TXN_002")
                    .requestId("REQ_002")
                    .description("转账出账")
                    .build();

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> service.adjustBalance(adjustment));
            assertEquals(AccountErrorCode.BALANCE_INSUFFICIENT.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("冻结/解冻")
    class FreezeUnfreezeTests {

        @Test
        @DisplayName("冻结应减少可用余额并增加冻结金额")
        void freezeShouldWorkCorrectly() {
            // Given
            when(accountBalanceRepository.findByAccountAndCurrency("ACC_001", "MOP")).thenReturn(testBalance);

            Money freezeAmount = Money.of(new BigDecimal("1000.00"), "MOP");

            // When
            boolean result = service.freezeBalance("ACC_001", freezeAmount, "FLOW_001", FreezeType.TRANSACTION, "转账冻结");

            // Then
            assertTrue(result);
            assertEquals(new BigDecimal("9000.00"), testBalance.getAvailableBalance());
            assertEquals(new BigDecimal("1000.00"), testBalance.getFrozenAmount());
            assertEquals(new BigDecimal("10000.00"), testBalance.getBalance()); // 总余额不变
            verify(accountBalanceRepository).save(testBalance);
            verify(accountFreezeLogRepository).save(any(AccountFreezeLog.class));
        }

        @Test
        @DisplayName("解冻应增加可用余额并减少冻结金额")
        void unfreezeShouldWorkCorrectly() {
            // Given
            testBalance.setAvailableBalance(new BigDecimal("9000.00"));
            testBalance.setFrozenAmount(new BigDecimal("1000.00"));
            when(accountBalanceRepository.findByAccountAndCurrency("ACC_001", "MOP")).thenReturn(testBalance);
            when(accountFreezeLogRepository.findByFlowNo("FLOW_001")).thenReturn(new AccountFreezeLog());

            Money unfreezeAmount = Money.of(new BigDecimal("1000.00"), "MOP");

            // When
            boolean result = service.unfreezeBalance("ACC_001", unfreezeAmount, "FLOW_001", "取消解冻");

            // Then
            assertTrue(result);
            assertEquals(new BigDecimal("10000.00"), testBalance.getAvailableBalance());
            assertEquals(new BigDecimal("0.00"), testBalance.getFrozenAmount());
            verify(accountBalanceRepository).save(testBalance);
        }

        @Test
        @DisplayName("解冻并扣款应减少冻结金额和总余额")
        void unfreezeAndDebitShouldWorkCorrectly() {
            // Given
            testBalance.setAvailableBalance(new BigDecimal("9000.00"));
            testBalance.setFrozenAmount(new BigDecimal("1000.00"));
            when(accountInfoRepository.findByAccountNo("ACC_001")).thenReturn(testAccountInfo);
            when(accountBalanceRepository.findByAccountAndCurrency("ACC_001", "MOP")).thenReturn(testBalance);
            when(accountSubLedgerRepository.findByRequestId(anyString())).thenReturn(null);
            when(accountFreezeLogRepository.findByFlowNo("FLOW_001")).thenReturn(new AccountFreezeLog());

            Money amount = Money.of(new BigDecimal("1000.00"), "MOP");

            // When
            boolean result = service.unfreezeAndDebit("ACC_001", amount, "FLOW_001", "转账确认",
                    BizType.TRANSFER_OUT, "REQ_003");

            // Then
            assertTrue(result);
            assertEquals(new BigDecimal("9000.00"), testBalance.getBalance());
            assertEquals(new BigDecimal("9000.00"), testBalance.getAvailableBalance());
            assertEquals(new BigDecimal("0.00"), testBalance.getFrozenAmount());
            verify(accountBalanceRepository).save(testBalance);
            verify(accountSubLedgerRepository).save(any(AccountSubLedger.class));
        }
    }
}
