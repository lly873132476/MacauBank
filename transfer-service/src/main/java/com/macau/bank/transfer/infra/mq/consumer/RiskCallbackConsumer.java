package com.macau.bank.transfer.infra.mq.consumer;

import com.macau.bank.common.core.constant.MqGroupConst;
import com.macau.bank.common.core.constant.MqTopicConst;
import com.macau.bank.common.core.enums.RiskStatus;
import com.macau.bank.transfer.domain.ability.TransferContextBuilder;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.factory.TransferStrategyFactory;
import com.macau.bank.transfer.domain.message.RiskResultMsg;
import com.macau.bank.transfer.domain.statemachine.StateMachineExecutor;
import com.macau.bank.transfer.domain.statemachine.StateTransition;
import com.macau.bank.transfer.domain.strategy.TransferStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 监听风控结果，断点续传驱动状态机
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = MqTopicConst.TP_RISK_CALLBACK, // 风控回调的 Topic
    consumerGroup = MqGroupConst.GP_TRANSFER_RISK_CALLBACK
)
public class RiskCallbackConsumer implements RocketMQListener<RiskResultMsg> {

    @Resource
    private StateMachineExecutor stateMachineExecutor;
    
    @Resource
    private TransferContextBuilder contextBuilder; // 你需要一个工具类从DB加载Context

    @Resource
    private TransferStrategyFactory strategyFactory;

    @Override
    public void onMessage(RiskResultMsg message) {
        log.info("收到风控回调消息: txnId={}, pass={}", message.getTxnId(), message.isPass());

        try {
            // 1. 恢复上下文 (从数据库查出 TransferOrder，重新构建 Context)
            TransferContext context = contextBuilder.rebuild(message.getTxnId());

            if (context == null) {
                log.warn("未找到对应交易单，可能已处理或数据异常: txnId={}", message.getTxnId());
                return;
            }

            // 2. 消费者负责找策略 (Consumer -> Factory)
            TransferStrategy strategy = strategyFactory.getStrategy(context.getOrder().getTransferType());

            // 3. 消费者负责问路
            StateTransition transition = strategy.getNextTransition(context.getOrder().getStatus(), message.isPass());

            // 4. 驱动状态机继续往下跑
            context.getOrder().setRiskStatus(message.isPass() ? RiskStatus.PASSED : RiskStatus.REJECTED);
            context.getOrder().setFailReason(message.isPass() ? null : "风控审核拒绝");
            stateMachineExecutor.drive(context, transition);
            
        } catch (Exception e) {
            log.error("消费风控回调失败，等待重试", e);
            throw e; // 抛出异常，让 MQ 重试
        }
    }
}