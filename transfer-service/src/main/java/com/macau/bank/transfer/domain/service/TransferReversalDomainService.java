package com.macau.bank.transfer.domain.service;

import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.transfer.common.result.TransferErrorCode;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.factory.TransferStrategyFactory;
import com.macau.bank.transfer.domain.repository.TransferOrderRepository;
import com.macau.bank.transfer.domain.statemachine.StateMachineExecutor;
import com.macau.bank.transfer.domain.statemachine.StateTransition;
import com.macau.bank.transfer.domain.strategy.TransferStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 转账冲正领域服务
 * <p>
 * 负责处理转账订单的冲正/退款流程。
 * <p>
 * 设计说明：
 * - 与正向流程架构对称：策略定义冲正路径，状态机引擎驱动执行
 * - 通过 {@link TransferStrategy#getReversalTransition} 获取冲正配置
 * - 使用 {@link StateMachineExecutor} 驱动 Handler 链执行
 * <p>
 * 冲正策略由各转账策略类自行定义，例如：
 * - 行内转账：直接冲正（入账冲正 → 扣款冲正 → 手续费冲正）
 * - 跨境转账：可能需要额外通知 SWIFT 取消
 */
@Slf4j
@Service
public class TransferReversalDomainService {

    @Resource
    private TransferOrderRepository transferOrderRepository;

    @Resource
    private TransferStrategyFactory transferStrategyFactory;

    @Resource
    private StateMachineExecutor stateMachineExecutor;

    /**
     * 执行订单冲正
     * <p>
     * 流程：校验 → 获取策略 → 获取冲正配置 → 状态机驱动执行
     *
     * @param orderId        订单ID
     * @param reversalReason 冲正原因
     * @return 冲正后的订单
     */
    public TransferOrder reverseOrder(Long orderId, String reversalReason) {
        log.info("[冲正] 开始处理冲正请求: orderId={}, reason={}", orderId, reversalReason);

        // 1. 查询订单
        TransferOrder order = transferOrderRepository.findById(orderId);
        if (order == null) {
            log.error("[冲正] 订单不存在: orderId={}", orderId);
            throw new BusinessException(TransferErrorCode.ORDER_NOT_FOUND);
        }

        // 2. 校验订单状态
        validateReversalAllowed(order);

        // 3. 记录原状态，用于决定冲正路径
        TransferStatus originalStatus = order.getStatus();

        // 4. 先更新为 REVERSING 状态
        order.setStatus(TransferStatus.REVERSING);
        order.setFailReason("冲正原因: " + reversalReason);
        transferOrderRepository.save(order);

        // 5. 构建上下文
        TransferContext context = TransferContext.builder()
                .order(order)
                .build();

        // 6. 【核心】通过策略获取冲正配置（与正向流程架构对称）
        TransferStrategy strategy = transferStrategyFactory.getStrategy(order.getTransferType());
        StateTransition transition = strategy.getReversalTransition(originalStatus);

        if (transition == null) {
            log.warn("[冲正] 策略返回空配置，无需冲正操作: orderId={}, originalStatus={}", orderId, originalStatus);
            order.setStatus(TransferStatus.REVERSED);
            transferOrderRepository.save(order);
            return order;
        }

        log.info("[冲正] 策略配置: orderId={}, strategy={}, handlers={}, targetStatus={}",
                orderId, strategy.getTransferType(), transition.getHandlers(), transition.getNextStatus());

        // 7. 使用状态机引擎驱动执行
        try {
            stateMachineExecutor.drive(context, transition);
            log.info("[冲正] 冲正成功: orderId={}", orderId);
        } catch (Exception e) {
            log.error("[冲正] 冲正失败，订单挂起: orderId={}, error={}", orderId, e.getMessage(), e);
            // 状态机内部会回滚，这里需要单独更新为挂起状态
            order.setStatus(TransferStatus.PENDING_COMPENSATION);
            order.setFailReason("冲正失败: " + e.getMessage());
            transferOrderRepository.save(order);
            throw e;
        }

        // 8. 返回最新订单状态
        return transferOrderRepository.findById(orderId);
    }

    /**
     * 校验是否允许冲正
     */
    private void validateReversalAllowed(TransferOrder order) {
        TransferStatus status = order.getStatus();

        // 已冲正的订单不能再次冲正
        if (status == TransferStatus.REVERSED || status == TransferStatus.REFUNDED) {
            log.error("[冲正] 订单已冲正，不能重复操作: orderId={}", order.getId());
            throw new BusinessException(TransferErrorCode.REVERSAL_ALREADY_DONE);
        }

        // 正在冲正的订单不能再次冲正
        if (status == TransferStatus.REVERSING) {
            log.error("[冲正] 订单正在冲正中: orderId={}", order.getId());
            throw new BusinessException(TransferErrorCode.REVERSAL_IN_PROGRESS);
        }
    }
}
