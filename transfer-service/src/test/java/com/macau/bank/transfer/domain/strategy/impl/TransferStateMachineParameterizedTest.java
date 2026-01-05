package com.macau.bank.transfer.domain.strategy.impl;

import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.statemachine.StateTransition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 转账状态机参数化测试
 * <p>
 * 致敬 ACTS 数据驱动：用参数化测试覆盖状态机的所有分支路径
 * <p>
 * 测试策略思考：
 * - 状态机有 N 个状态，每个状态可能因 isRiskPass 不同走向不同分支
 * - 一个方法 + CSV 数据 = 覆盖所有状态转换
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("转账状态机参数化测试 (ACTS 风格)")
class TransferStateMachineParameterizedTest {

    @Mock
    private AccountGateway accountGateway;

    @InjectMocks
    private InternalTransferStrategy strategy;

    @DisplayName("状态转换测试 - 所有路径覆盖")
    @ParameterizedTest(name = "[{index}] {0} + 风控={1} => 目标状态={2}, Handler数={3}")
    @CsvSource({
            // 当前状态, 风控结果, 预期目标状态, 预期Handler数量, 是否返回null
            "INIT,             true,     PENDING_RISK,   2,               false",
            "INIT,             false,    PENDING_RISK,   2,               false",
            "PENDING_RISK,     true,     SUCCESS,        3,               false",
            "PENDING_RISK,     false,    null,           0,               true",
            "SUCCESS,          true,     null,           0,               true",
            "FAILED,           true,     null,           0,               true"
    })
    void test_state_transition(String currentStatusStr,
            boolean isRiskPass,
            String expectedTargetStatusStr,
            int expectedHandlerCount,
            boolean expectNull) {

        // 1. 准备数据
        TransferStatus currentStatus = TransferStatus.valueOf(currentStatusStr);

        // 2. 执行
        StateTransition transition = strategy.getNextTransition(currentStatus, isRiskPass);

        // 3. 验证
        if (expectNull) {
            assertNull(transition,
                    "状态 [" + currentStatusStr + "] + 风控=" + isRiskPass + " 应该返回 null");
        } else {
            assertNotNull(transition,
                    "状态 [" + currentStatusStr + "] + 风控=" + isRiskPass + " 不应该返回 null");

            TransferStatus expectedTargetStatus = TransferStatus.valueOf(expectedTargetStatusStr);
            assertEquals(expectedTargetStatus, transition.getNextStatus(),
                    "目标状态不匹配");
            assertEquals(expectedHandlerCount, transition.getHandlers().size(),
                    "Handler 数量不匹配");
        }
    }

    @DisplayName("冲正流程测试 - 按原始状态分派")
    @ParameterizedTest(name = "[{index}] 原状态={0} => 目标={1}, Handler数={2}")
    @CsvSource({
            // 原始状态, 预期目标状态, 预期Handler数量, 是否返回null
            "SUCCESS,          REVERSED,     3,               false",
            "FAILED,           REVERSED,     1,               false",
            "INIT,             REVERSED,     1,               false",
            "PENDING_RISK,     REVERSED,     1,               false",
            "REVERSED,         null,         0,               true"
    })
    void test_reversal_transition(String originalStatusStr,
            String expectedTargetStatusStr,
            int expectedHandlerCount,
            boolean expectNull) {

        // 1. 准备数据
        TransferStatus originalStatus = TransferStatus.valueOf(originalStatusStr);

        // 2. 执行
        StateTransition transition = strategy.getReversalTransition(originalStatus);

        // 3. 验证
        if (expectNull) {
            assertNull(transition,
                    "原状态 [" + originalStatusStr + "] 的冲正应该返回 null");
        } else {
            assertNotNull(transition,
                    "原状态 [" + originalStatusStr + "] 的冲正不应该返回 null");

            TransferStatus expectedTargetStatus = TransferStatus.valueOf(expectedTargetStatusStr);
            assertEquals(expectedTargetStatus, transition.getNextStatus(),
                    "冲正目标状态不匹配");
            assertEquals(expectedHandlerCount, transition.getHandlers().size(),
                    "冲正 Handler 数量不匹配");
        }
    }
}
