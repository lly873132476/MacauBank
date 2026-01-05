package com.macau.bank.auth.interfaces.http.controller;

import com.macau.bank.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth/code")
@Tag(name = "验证码服务", description = "短信验证码发送接口")
public class VerifyCodeController {

    @Operation(summary = "发送验证码")
    @PostMapping("/send")
    public Result<Void> sendVerifyCode(@RequestParam String mobile) {
        // 在真实场景中，这里会调用 SMS 服务商接口 (如阿里云、Twilio)
        // 并将验证码存入 Redis (Key: "sms:code:" + mobile, TTL: 5min)
        
        String mockCode = "123456";
        log.info("【模拟短信网关】向手机号 {} 发送验证码: {}", mobile, mockCode);
        
        // 为了方便演示，这里直接打印日志，并不真正发送
        return Result.success();
    }
}
