package com.macau.bank.transfer.domain.service;

import com.macau.bank.common.core.enums.Deleted;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.model.AccountSnapshot;
import com.macau.bank.transfer.domain.repository.TransferOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 转账订单领域服务测试
 * <p>
 * 测试目标：验证转账订单的创建、状态更新、查询等核心流程
 * <p>
 * 测试策略：
 * 1. Mock Repository，验证 Service 方法的业务逻辑
 * 2. 参数化测试覆盖所有状态转换场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("转账订单领域服务测试")
class TransferOrderDomainServiceTest {

    @Mock
    private TransferOrderRepository transferOrderRepository;

    @InjectMocks
    private TransferOrderDomainService service;

    private TransferOrder testOrder;
    private TransferContext testContext;

    @BeforeEach
    void setUp() {
        testOrder = new TransferOrder();
        testOrder.setPayerAccountNo("888001");
        testOrder.setPayeeAccountNo("888002");
        testOrder.setAmount(new BigDecimal("1000.00"));
        testOrder.setCurrencyCode("MOP");

        AccountSnapshot payerAccount = new AccountSnapshot();
        payerAccount.setAccountNo("888001");
        payerAccount.setAccountName("张三");

        testContext = TransferContext.builder()
                .order(testOrder)
                .payerAccount(payerAccount)
                .build();
    }

    @Nested
    @DisplayName("订单创建")
    class OrderCreationTests {

        @Test
        @DisplayName("创建订单应生成 txnId 并设置初始状态")
        void shouldCreateOrderWithTxnIdAndInitStatus() {
            // When
            String txnId = service.createOrder(testContext);

            // Then
            assertNotNull(txnId);
            assertTrue(txnId.startsWith("TR"));
            assertEquals(TransferStatus.INIT, testContext.getOrder().getStatus());
            assertEquals(Deleted.NO_DELETED, testContext.getOrder().getDeleted());
            assertEquals(Integer.valueOf(0), testContext.getOrder().getVersion());
            verify(transferOrderRepository).save(any(TransferOrder.class));
        }

        @Test
        @DisplayName("创建订单应填充付款方信息")
        void shouldFillPayerInfo() {
            // When
            service.createOrder(testContext);

            // Then
            assertEquals("888001", testContext.getOrder().getPayerAccountNo());
            assertEquals("张三", testContext.getOrder().getPayerAccountName());
        }
    }

    @Nested
    @DisplayName("状态更新 (参数化)")
    class StatusUpdateTests {

        @DisplayName("状态更新场景覆盖")
        @ParameterizedTest(name = "[{index}] 从 {0} 更新到 {1}, 失败原因={2}")
        @CsvSource({
                // 原状态, 目标状态, 失败原因(null表示无)
                "INIT,           PENDING_RISK,   null",
                "PENDING_RISK,   SUCCESS,        null",
                "PENDING_RISK,   FAILED,         风控拒绝",
                "SUCCESS,        REVERSED,       用户申请冲正",
                "INIT,           FAILED,         余额不足"
        })
        void shouldUpdateStatus(String fromStatusStr,
                String toStatusStr,
                String failReason) {
            // Given
            TransferStatus fromStatus = TransferStatus.valueOf(fromStatusStr);
            TransferStatus toStatus = TransferStatus.valueOf(toStatusStr);
            String reason = "null".equals(failReason) ? null : failReason;

            TransferOrder existingOrder = new TransferOrder();
            existingOrder.setTxnId("TR_TEST_001");
            existingOrder.setStatus(fromStatus);
            when(transferOrderRepository.findByTxnId("TR_TEST_001")).thenReturn(existingOrder);

            // When
            service.updateStatus("TR_TEST_001", toStatus, reason);

            // Then
            assertEquals(toStatus, existingOrder.getStatus());
            if (reason != null) {
                assertEquals(reason, existingOrder.getFailReason());
            }
            verify(transferOrderRepository).save(existingOrder);
        }
    }

    @Nested
    @DisplayName("订单状态检查 (参数化)")
    class OrderStatusCheckTests {

        @DisplayName("checkOrderStatus 应正确判断成功状态")
        @ParameterizedTest(name = "[{index}] 状态={0}, 预期结果={1}")
        @CsvSource({
                // 订单状态, checkOrderStatus 返回值
                "SUCCESS,       true",
                "INIT,          false",
                "PENDING_RISK,  false",
                "FAILED,        false",
                "REVERSED,      false"
        })
        void shouldReturnCorrectStatusCheck(String statusStr, boolean expectedResult) {
            // Given
            TransferStatus status = TransferStatus.valueOf(statusStr);
            TransferOrder order = new TransferOrder();
            order.setTxnId("TR_TEST_002");
            order.setStatus(status);
            when(transferOrderRepository.findByTxnId("TR_TEST_002")).thenReturn(order);

            // When
            boolean result = service.checkOrderStatus("TR_TEST_002");

            // Then
            assertEquals(expectedResult, result);
        }

        @Test
        @DisplayName("订单不存在时应返回 false")
        void shouldReturnFalseWhenOrderNotFound() {
            // Given
            when(transferOrderRepository.findByTxnId("NON_EXIST")).thenReturn(null);

            // When
            boolean result = service.checkOrderStatus("NON_EXIST");

            // Then
            assertFalse(result);
        }
    }
}
