package com.macau.bank.message.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macau.bank.common.core.constant.MqGroupConst;
import com.macau.bank.common.core.constant.MqTopicConst;
import com.macau.bank.common.core.enums.UserLevel;
import com.macau.bank.message.domain.service.MessageDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户安全与等级变动消费者 - 基于模版优化版
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqTopicConst.TP_USER_SECURITY, consumerGroup = MqGroupConst.GP_MESSAGE_SERVICE_USER)
public class UserLevelMessageConsumer implements RocketMQListener<String> {

    @Resource
    private MessageDomainService messageDomainService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(String message) {
        log.info("接收到用户安全事件消息: {}", message);
        try {
            // 1. 定义一个“一次性”的结构体 (JDK 14+ Record)
            // Jackson 会自动把 String "GOLD" 转成 UserLevel.GOLD
            @JsonIgnoreProperties(ignoreUnknown = true)
            record TempDto(String userNo, UserLevel userLevel) {
            }

            TempDto dto = objectMapper.readValue(message, TempDto.class);

            if (dto == null || dto.userNo() == null || dto.userLevel() == null)
                return;

            String userNo = dto.userNo();
            UserLevel userLevel = dto.userLevel();

            // 1. 准备模版参数 (对齐 init.sql 中的 {level} 占位符)
            Map<String, Object> params = new HashMap<>();
            params.put("level", userLevel.getCode());

            // 2. 调用领域服务按模版发送 (对齐 MSG_USER_LEVEL_UP)
            messageDomainService.createMessageByTemplate(userNo, "MSG_USER_LEVEL_UP", params,
                    userNo + "_" + userLevel.name());

            log.info("用户等级变更消息处理完成: userNo={}, newLevel={}", userNo, userLevel);

        } catch (Exception e) {
            log.error("消费用户安全事件失败", e);
        }
    }
}