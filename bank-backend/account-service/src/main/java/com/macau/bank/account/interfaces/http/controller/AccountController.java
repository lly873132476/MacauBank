package com.macau.bank.account.interfaces.http.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.account.application.query.AccountListQuery;
import com.macau.bank.account.application.query.AssetSummaryQuery;
import com.macau.bank.account.application.query.TransactionFlowQuery;
import com.macau.bank.account.application.result.AccountInfoResult;
import com.macau.bank.account.application.result.AssetSummaryResult;
import com.macau.bank.account.application.result.TransactionFlowResult;
import com.macau.bank.account.application.service.AccountAppService;
import com.macau.bank.account.common.result.AccountErrorCode;
import com.macau.bank.account.interfaces.http.assembler.AccountWebAssembler;
import com.macau.bank.account.interfaces.http.assembler.TransactionFlowWebAssembler;
import com.macau.bank.account.interfaces.http.request.TransactionFlowRequest;
import com.macau.bank.account.interfaces.http.response.AccountResponse;
import com.macau.bank.account.interfaces.http.response.AssetSummaryResponse;
import com.macau.bank.account.interfaces.http.response.TransactionFlowResponse;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.common.framework.web.annotation.CurrentUser;
import com.macau.bank.common.framework.web.context.RequestHeaderContext;
import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账户核心业务接口 (面向App端)
 */
@Slf4j
@Tag(name = "账户业务", description = "提供资产总览、账户列表等首页核心功能")
@RestController
@RequestMapping("/account")
public class AccountController {

    @Resource
    private AccountAppService accountAppService;

    @Resource
    private AccountWebAssembler accountWebAssembler;

    @Resource
    private TransactionFlowWebAssembler transactionFlowWebAssembler;

    /**
     * 获取首页资产总览
     * 包括等值澳门元总资产和简要账户列表
     */
    @Operation(summary = "获取资产总览")
    @GetMapping("/asset/summary")
    public Result<AssetSummaryResponse> getAssetSummary(@CurrentUser String userNo) {
        log.info("App端获取资产总览: userNo={}", userNo);
        
        AssetSummaryQuery query = AssetSummaryQuery.builder()
                .userNo(userNo)
                .build();
        
        AssetSummaryResult result = accountAppService.getAssetSummary(query);
        return Result.success(accountWebAssembler.toResponse(result));
    }

    /**
     * 获取账户列表详情
     * 用于"我的账户"页面，展示完整的账户和各币种余额
     */
    @Operation(summary = "获取账户列表")
    @GetMapping("/list")
    public Result<List<AccountResponse>> getAccountList(@CurrentUser String userNo) {
        log.info("App端获取账户列表: userNo={}", userNo);
        
        AccountListQuery query = AccountListQuery.builder()
                .userNo(userNo)
                .build();
                
        List<AccountInfoResult> resultList = accountAppService.getAccountList(query);
        return Result.success(accountWebAssembler.toResponseList(resultList));
    }

    /**
     * 获取交易流水列表
     * 支持分页及多种条件筛选
     */
    @Operation(summary = "获取交易流水")
    @PostMapping("/bill/list")
    public Result<Page<TransactionFlowResponse>> getTransactionFlows(
            @RequestBody @Validated TransactionFlowRequest request) {
        
        // 1. 从上下文获取用户信息，防止 NPE
        BaseRequest ctx = RequestHeaderContext.get();
        if (ctx == null || ctx.getUserNo() == null) {
            log.warn("用户上下文缺失");
            throw new BusinessException(AccountErrorCode.USER_CONTEXT_MISSING);
        }
        String userNo = ctx.getUserNo();
        
        // 2. 日志脱敏：账户号只显示后4位
        String maskedAccountNo = maskAccountNo(request.getAccountNo());
        log.info("App端查询交易流水: userNo={}, accountNo={}", userNo, maskedAccountNo);
        
        // 3. 账户归属校验：确保账户属于当前用户
        if (StringUtils.hasText(request.getAccountNo())) {
            boolean isOwner = accountAppService.validateAccountOwnership(request.getAccountNo(), userNo);
            if (!isOwner) {
                log.warn("账户归属校验失败: accountNo={}, userNo={}", maskedAccountNo, userNo);
                throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_BELONG_TO_USER);
            }
        }
        
        // 4. 构建查询并执行
        TransactionFlowQuery query = transactionFlowWebAssembler.toQuery(request);
        query.setUserNo(userNo);
        
        Page<TransactionFlowResult> resultPage = accountAppService.getTransactionFlows(query);
        
        // 5. 转换 Result -> Response
        Page<TransactionFlowResponse> responsePage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        responsePage.setRecords(transactionFlowWebAssembler.toResponseList(resultPage.getRecords()));
        
        return Result.success(responsePage);
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