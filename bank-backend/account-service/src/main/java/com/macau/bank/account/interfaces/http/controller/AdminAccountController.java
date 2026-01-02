package com.macau.bank.account.interfaces.http.controller;

import com.macau.bank.account.application.command.AdminDepositCmd;
import com.macau.bank.account.application.command.AdminOpenCurrencyAccountCmd;
import com.macau.bank.account.application.service.AccountAppService;
import com.macau.bank.account.interfaces.http.assembler.AccountWebAssembler;
import com.macau.bank.account.interfaces.http.dto.AdminDepositRequest;
import com.macau.bank.account.interfaces.http.dto.AdminOpenCurrencyRequest;
import com.macau.bank.common.core.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账户管理后台接口
 * <p>
 * DDD 分层职责：
 * - 只负责 HTTP 请求的接收和响应
 * - 不包含任何业务逻辑
 * - 所有业务逻辑委托给 Application Service
 */
@Slf4j
@RestController
@RequestMapping("/account/admin")
@Validated
public class AdminAccountController {

    @Resource
    private AccountAppService accountAppService;

    @Resource
    private AccountWebAssembler accountWebAssembler;

    /**
     * 后台充值接口
     * <p>提供给 APIPost 使用，用于测试和管理</p>
     */
    @PostMapping("/deposit")
    public Result<Boolean> deposit(@RequestBody @Validated AdminDepositRequest request) {
        // 日志脱敏：不打印敏感信息（adminSecret、完整金额等）
        log.info("收到后台充值请求: userNo={}, currencyCode={}", 
                request.getUserNo(), request.getCurrencyCode());

        // 1. 【拆包】 Request -> Cmd
        AdminDepositCmd cmd = accountWebAssembler.toCmd(request);

        // 2. 【办事】 调用应用层服务
        boolean success = accountAppService.adminDeposit(cmd);
        
        // 3. 记录操作结果日志
        log.info("后台充值完成: userNo={}, currencyCode={}, success={}", 
                request.getUserNo(), request.getCurrencyCode(), success);
        
        // 4. 【包装】 Result -> Response
        return Result.success(success);
    }

    /**
     * 后台开通币种账户接口
     * <p>用于给已有账户增加新的币种余额记录 (AccountBalance)。
     * 操作是幂等的：如果该币种已存在，则直接返回成功。</p>
     *
     * @param request 包含账号和币种代码的请求体
     * @return 操作结果 (true 表示成功或已存在)
     */
    @PostMapping("/open-currency")
    public Result<Boolean> openCurrencyAccount(@RequestBody @Validated AdminOpenCurrencyRequest request) {
        // 日志脱敏：只打印必要字段，不打印 adminSecret
        log.info("收到后台开通币种请求: accountNo={}, currencyCode={}", 
                maskAccountNo(request.getAccountNo()), request.getCurrencyCode());

        // 1. 【拆包】 Request -> Cmd
        AdminOpenCurrencyAccountCmd cmd = accountWebAssembler.toCmd(request);

        // 2. 【办事】 调用应用层服务
        boolean success = accountAppService.openCurrencyAccount(cmd);
        
        // 3. 记录操作结果日志
        log.info("后台开通币种完成: accountNo={}, currencyCode={}, success={}", 
                maskAccountNo(request.getAccountNo()), request.getCurrencyCode(), success);
        
        // 4. 【包装】 Result -> Response
        return Result.success(success);
    }
    
    /**
     * 账户号脱敏：只显示后4位
     */
    private String maskAccountNo(String accountNo) {
        if (accountNo == null || accountNo.length() <= 4) {
            return "****";
        }
        return "****" + accountNo.substring(accountNo.length() - 4);
    }
}
