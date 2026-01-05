package com.macau.bank.transfer.interfaces.http.controller;

import com.macau.bank.common.core.result.Result;
import org.springframework.web.bind.annotation.*;
import java.util.Random;

/**
 * 模拟外部银行接口 (SWIFT/支付宝)
 * 特点：慢、不稳定
 */
@RestController
@RequestMapping("/mock/external-bank")
public class ExternalBankController {

    @PostMapping("/process")
    public Result<String> processTransfer(@RequestBody String body) throws InterruptedException {
        // 1. 模拟网络延迟 (2秒)
        // 这就是为什么不能用 Seata AT 的原因：这里睡2秒，数据库锁就要卡2秒，并发直接归零。
        Thread.sleep(2000);

        // 2. 模拟随机失败 (20% 概率掉单)
        if (new Random().nextInt(10) < 2) {
            return Result.fail("外部银行系统维护中 (模拟失败)");
        }

        // 3. 成功
        return Result.success("受理成功: " + System.currentTimeMillis());
    }
}