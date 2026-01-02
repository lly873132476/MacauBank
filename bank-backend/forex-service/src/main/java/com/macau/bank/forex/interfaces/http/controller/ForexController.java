package com.macau.bank.forex.interfaces.http.controller;

import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.common.framework.web.context.RequestHeaderContext;
import com.macau.bank.common.framework.web.model.BaseRequest;
import com.macau.bank.forex.application.command.ExchangeCmd;
import com.macau.bank.forex.application.result.ExchangeResult;
import com.macau.bank.forex.application.service.ForexAppService;
import com.macau.bank.forex.common.result.ForexErrorCode;
import com.macau.bank.forex.interfaces.http.assembler.ForexWebAssembler;
import com.macau.bank.forex.interfaces.http.request.ForexExchangeRequest;
import com.macau.bank.forex.interfaces.http.response.ForexExchangeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "外汇交易", description = "提供实时外币兑换服务")
@RestController
@RequestMapping("/forex")
public class ForexController {

    @Resource
    private ForexAppService forexAppService;

    @Resource
    private ForexWebAssembler forexWebAssembler;

    /**
     * 外币兑换
     */
    @Operation(summary = "外币兑换")
    @PostMapping("/exchange")
    public Result<ForexExchangeResponse> exchange(@RequestBody @Validated ForexExchangeRequest request) {
        // 1. 日志脱敏：仅记录关键字段，不暴露账户号等敏感信息
        log.info("接收到外币兑换请求: requestId={}, pairCode={}, sellCurrency={}, sellAmount={}, buyCurrency={}",
                request.getRequestId(), request.getPairCode(), request.getSellCurrency(), 
                request.getSellAmount(), request.getBuyCurrency());
        
        // 2. 从上下文获取用户信息，防止 NPE
        BaseRequest ctx = RequestHeaderContext.get();
        if (ctx == null || ctx.getUserNo() == null) {
            log.warn("用户上下文缺失，requestId={}", request.getRequestId());
            throw new BusinessException(ForexErrorCode.USER_CONTEXT_MISSING);
        }
        String userNo = ctx.getUserNo();
        
        // 3. Request -> Cmd
        ExchangeCmd cmd = forexWebAssembler.toCmd(request);
        cmd.setUserNo(userNo);
        
        // 4. 执行交易（账户归属校验和幂等性检查在 AppService 层实现）
        ExchangeResult result = forexAppService.exchange(cmd);

        // 5. 记录交易成功日志
        log.info("外币兑换成功: requestId={}, txnId={}, userNo={}", 
                request.getRequestId(), result.getTxnId(), userNo);

        // 6. Result -> Response
        return Result.success(forexWebAssembler.toResponse(result));
    }
}
