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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StateMachineExecutor {

    @Resource
    private TransactionTemplate transactionTemplate; // 编程式事务核心
    
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
     * @param context 上下文
     * @param transition 流程配置
     */
    public void drive(TransferContext context, StateTransition transition) {
        TransferStatus currentStatus = context.getOrder().getStatus();

        if (transition == null) {
            log.warn("状态机停止：当前状态无后续路径");
            return;
        }

        // 1. 【核心】在一个新事务中执行所有 Handler + 更新状态
        transactionTemplate.execute(status -> {
            try {
                log.info(">>> 状态机启动: {} -> {}", currentStatus, transition.getNextStatus());

                // 1.1 执行 Handler 链
                for (TransferPhaseEnum phase : transition.getHandlers()) {
                    TransferHandler handler = handlerMap.get(phase);
                    if (handler != null) {
                        handler.handle(context);
                    }
                }

                // 1.2 推进状态
                context.getOrder().setStatus(transition.getNextStatus());
                orderDomainService.updateOrder(context.getOrder());

                return null;
            } catch (Exception e) {
                // 1.3 异常回滚：Handler 操作和状态更新一起回滚
                status.setRollbackOnly();
                log.error("状态机执行异常，事务回滚", e);
                throw e;
            }
        });
    }
}