package com.macau.bank.message.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.message.domain.MessageDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 核心交易消息消费者 - 基于模版优化版
 * 遵循 init.sql 中 message_template 的设计初衷
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "BANK_TXN_EVENT_TOPIC", consumerGroup = "message-service-txn-group")
public class TransactionMessageConsumer implements RocketMQListener<String> {

    @Resource
    private MessageDomainService messageDomainService;

    @Resource
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void onMessage(String message) {
        log.info("接收到银行交易事件消息: {}", message);

        try {
            @JsonIgnoreProperties(ignoreUnknown = true)
            record TempDto(String userNo, String txnId, BizType bizType, BigDecimal amount, String currency) {}
            TempDto dto = objectMapper.readValue(message, TempDto.class);
            String userNo = dto.userNo();
            String txnId = dto.txnId();
            BizType bizType = dto.bizType();
            BigDecimal amount = dto.amount();
            String currency = dto.currency();

            if (!StringUtils.hasText(userNo) || !StringUtils.hasText(txnId)) return;

            // 1. 幂等性校验 (通过 DomainService)
            if (messageDomainService.isMessageExists(txnId, userNo)) {
                log.info("消息已处理，跳过: txnId={}", txnId);
                return;
            }

            // 2. 准备模版参数
            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount.abs().toPlainString());
            params.put("currency", currency);
            params.put("txnId", txnId);
            params.put("type", (amount.compareTo(BigDecimal.ZERO) > 0) ? "存入" : "支出");

            // 3. 根据业务类型选择模版 (对齐 init.sql)
            String templateCode = (bizType == BizType.FOREX_EXCHANGE) ? "MSG_FOREX_SUCCESS" : "MSG_TRANS_SUCCESS";

            // 4. 调用领域服务按模版发送 (支持多渠道)
            messageDomainService.createMessageByTemplate(userNo, templateCode, params, txnId);

            log.info("基于模版的消息处理完成: userNo={}, template={}", userNo, templateCode);

        } catch (Exception e) {
            log.error("消费交易事件失败: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}