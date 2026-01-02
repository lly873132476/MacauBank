package com.macau.bank.account.application.listener;

import com.macau.bank.account.application.command.CreateAccountCmd;
import com.macau.bank.account.application.service.AccountAppService;
import com.macau.bank.common.core.constant.MqGroupConst;
import com.macau.bank.common.core.constant.MqTopicConst;
import com.macau.bank.common.core.enums.AccountType;
import com.macau.bank.common.core.enums.Currency;
import com.macau.bank.common.event.UserAuditedEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 用户审核通过 MQ 监听器
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = MqTopicConst.TP_USER_AUDIT_PASS,
    consumerGroup = MqGroupConst.GP_ACCOUNT_USER_AUDIT
)
public class UserAuditMqListener implements RocketMQListener<UserAuditedEvent> {

    @Resource
    private AccountAppService accountAppService;

    @Override
    public void onMessage(UserAuditedEvent event) {
        log.info("收到 MQ 消息 - 准备开户: userNo={}", event.getUserNo());

        try {
            CreateAccountCmd createAccountCmd = CreateAccountCmd.builder()
                    .userNo(event.getUserNo())
                    .accountType(AccountType.SAVINGS)
                    .initialCurrencyCode(Currency.MOP.getCode())
                    .initialBalance(BigDecimal.ZERO)
                    .build();

            accountAppService.createAccount(createAccountCmd);
            log.info("MQ 消费成功 - 开户完成: userNo={}", event.getUserNo());
            
        } catch (Exception e) {
            log.error("MQ 消费失败，正在重试: userNo={}", event.getUserNo(), e);
            throw new RuntimeException("开户失败", e);
        }
    }
}