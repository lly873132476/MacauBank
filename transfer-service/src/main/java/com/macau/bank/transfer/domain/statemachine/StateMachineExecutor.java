package com.macau.bank.transfer.domain.statemachine;

import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import com.macau.bank.transfer.domain.service.TransferOrderDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StateMachineExecutor {

    @Resource
    private TransferOrderDomainService orderDomainService;

    // 自动注入所有 Handler
    private final Map<TransferPhaseEnum, TransferHandler> handlerMap = new EnumMap<>(TransferPhaseEnum.class);

    @Autowired
    public void setHandlers(List<TransferHandler> handlers) {
        handlers.forEach(h -> handlerMap.put(h.getPhase(), h));
    }

    /**
     * 驱动引擎运行
     * <p>
     * 优化说明:
     * 1. 移除 TransactionTemplate: Handler 是 RPC 调用,不受本地事务控制
     * 2. 单次 UPDATE 操作,MyBatis 自动提交,无需显式事务
     * 3. 异常处理: 不改状态,保持当前状态,避免资损
     *
     * @param context    上下文
     * @param transition 流程配置
     */
    public void drive(TransferContext context, StateTransition transition) {
        TransferStatus currentStatus = context.getOrder().getStatus();

        if (transition == null) {
            log.warn("状态机停止:当前状态无后续路径, currentStatus={}", currentStatus);
            return;
        }

        try {
            log.info(">>> 状态机启动: {} -> {}, txnId={}",
                    currentStatus, transition.getNextStatus(), context.getOrder().getTxnId());

            // 1. 执行 Handler 链 (RPC 调用,不需要事务)
            for (TransferPhaseEnum phase : transition.getHandlers()) {
                TransferHandler handler = handlerMap.get(phase);
                if (handler != null) {
                    log.debug("执行 Handler: phase={}, txnId={}", phase, context.getOrder().getTxnId());
                    handler.handle(context);
                }
            }

            // 2. 更新状态 (单次 DB 操作,MyBatis 自动提交)
            context.getOrder().setStatus(transition.getNextStatus());
            orderDomainService.saveOrder(context.getOrder());

            log.info("<<< 状态机完成: {} -> {}, txnId={}",
                    currentStatus, transition.getNextStatus(), context.getOrder().getTxnId());

        } catch (Exception e) {
            // 3. 异常处理: 不改状态,保持当前状态
            // 原因: Handler 可能部分成功 (如 Freeze 成功但 SendMQ 失败)
            // 直接标记失败会导致资损 (资金已冻结但订单显示失败)
            // 正确做法: 保持当前状态,让 MQ 重试或人工介入
            log.error("状态机执行失败,保持当前状态: currentStatus={}, txnId={}, error={}",
                    currentStatus, context.getOrder().getTxnId(), e.getMessage(), e);
            throw e;
        }
    }
}