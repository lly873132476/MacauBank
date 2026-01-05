package com.macau.bank.transfer.application.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.transfer.application.command.TransferCmd;
import com.macau.bank.transfer.application.result.TransferResult;
import com.macau.bank.transfer.common.result.TransferErrorCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Sentinel 降级处理类
 * <p>
 * 当转账服务触发限流或熔断时，提供快速失败的降级响应。
 * <p>
 * 设计说明：
 * - blockHandler: 处理 Sentinel 限流/熔断（BlockException）
 * - fallback: 处理业务异常时的降级逻辑
 */
@Slf4j
public class TransferSentinelFallback {

    /**
     * 限流/熔断时的降级处理
     * <p>
     * 当 QPS 超过阈值或触发熔断规则时，Sentinel 会抛出 BlockException，
     * 此方法会立即返回"系统繁忙"，快速失败，保护后端服务。
     *
     * @param cmd 转账指令
     * @param ex  Sentinel 阻塞异常
     * @return 永远不会返回，直接抛出业务异常
     */
    public static TransferResult submitTransferBlockHandler(TransferCmd cmd, BlockException ex) {
        log.warn("[Sentinel] 转账接口被限流或熔断: idempotentKey={}, rule={}",
                cmd.getIdempotentKey(), ex.getRule());
        throw new BusinessException(TransferErrorCode.SYSTEM_BUSY);
    }

    /**
     * 异常时的降级处理
     * <p>
     * 当业务逻辑抛出非 BlockException 的异常时，此方法提供兜底响应。
     * 注意：只有在配置了 exceptionsToIgnore 之外的异常时才会触发。
     *
     * @param cmd       转账指令
     * @param throwable 业务异常
     * @return 永远不会返回，直接抛出业务异常
     */
    public static TransferResult submitTransferFallback(TransferCmd cmd, Throwable throwable) {
        // 如果是业务异常，直接抛出，不做降级处理
        if (throwable instanceof BusinessException) {
            throw (BusinessException) throwable;
        }

        log.error("[Sentinel] 转账接口异常降级: idempotentKey={}, error={}",
                cmd.getIdempotentKey(), throwable.getMessage(), throwable);
        throw new BusinessException(TransferErrorCode.SYSTEM_ERROR);
    }
}
