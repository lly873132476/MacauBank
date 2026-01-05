package com.macau.bank.transfer.interfaces.http.controller;

import com.macau.bank.common.core.constant.MqTopicConst;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.transfer.domain.message.RiskResultMsg;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/mock/risk")
// @Profile("dev") // 加上这个，生产环境自动禁用，防事故
public class DevToolController {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 模拟风控回调
     * 用法：GET /dev/mock/risk-callback?txnId=TX123456&pass=true
     */
    @GetMapping("/callback")
    public Result<String> mockRiskCallback(@RequestParam String txnId,
                                           @RequestParam(defaultValue = "true") boolean pass) {
        
        log.info("😈 [开发后门] 手动触发风控回调: txnId={}, pass={}", txnId, pass);

        // 1. 构造消息
        RiskResultMsg msg = RiskResultMsg.builder()
                .txnId(txnId)
                .isPass(pass)
                .reason(pass ? "Mock Pass" : "Mock Reject")
                .build();

        // 2. 发送消息到 Topic (模拟风控系统发出)
        // 你的 Listener 会监听到这条消息，然后驱动状态机
        rocketMQTemplate.convertAndSend(MqTopicConst.TP_RISK_CALLBACK, msg);

        return Result.success("Mock消息已发送，请查看控制台日志");
    }
}