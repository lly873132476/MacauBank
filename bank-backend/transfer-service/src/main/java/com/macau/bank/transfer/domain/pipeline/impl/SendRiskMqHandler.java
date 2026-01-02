package com.macau.bank.transfer.domain.pipeline.impl;

import com.macau.bank.common.core.constant.MqTopicConst;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.message.RiskRequestMsg;
import com.macau.bank.transfer.domain.pipeline.TransferHandler;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

/**
 * 风控消息发送处理器
 * <p>
 * 职责：发送风控检查请求消息至 MQ，触发异步风控校验
 * <p>
 * 阶段：{@link TransferPhaseEnum#SEND_RISK_MQ}
 * <p>
 * 设计说明：
 * - 异步断点：发送消息后当前流程暂停，等待风控回调继续
 * - 消息体包含 txnId 用于回调时关联订单
 */
@Slf4j
@Component
public class SendRiskMqHandler implements TransferHandler {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public TransferPhaseEnum getPhase() {
        return TransferPhaseEnum.SEND_RISK_MQ;
    }

    @Override
    public void handle(TransferContext context) {
        log.info("阶段 [SendRiskMq]: 准备发送风控申请, txnId={}", context.getOrder().getTxnId());

        // 构造消息体 (必须包含 txnId 以便回调接力)
        RiskRequestMsg msg = new RiskRequestMsg();
        msg.setTxnId(context.getOrder().getTxnId());
        msg.setAccountNo(context.getOrder().getPayerAccountNo());
        msg.setAmount(context.getAmount());
        msg.setTargetCountry("MO");

        // 发送普通消息 (因为本地事务马上就提交了，这里不需要再用事务消息了)
        rocketMQTemplate.convertAndSend(MqTopicConst.TP_RISK_CHECK_REQUEST, msg);

        log.info("风控消息已发送，当前线程任务结束，等待回调...");
    }
}