package com.macau.bank.forex.interfaces.http.controller;

import com.macau.bank.common.core.result.Result;
import com.macau.bank.forex.application.service.CurrencyAppService;
import com.macau.bank.forex.domain.entity.ExchangeRate;
import com.macau.bank.forex.interfaces.http.assembler.CurrencyWebAssembler;
import com.macau.bank.forex.interfaces.http.response.ExchangeRateReferenceResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 汇率Controller
 */
@Slf4j
@Tag(name = "汇率基础", description = "提供汇率相关服务")
@RestController
@RequestMapping("/currency")
public class CurrencyController {

    @Resource
    private CurrencyAppService currencyAppService;

    @Resource
    private CurrencyWebAssembler currencyWebAssembler;

    /**
     * 获取首页参考汇率列表
     */
    @GetMapping("/reference/list")
    public Result<List<ExchangeRateReferenceResponse>> getReferenceList() {
        log.info("控制器 - 获取参考汇率列表");
        List<ExchangeRate> rates = currencyAppService.getReferenceRates();
        return Result.success(currencyWebAssembler.toReferenceResponseList(rates));
    }

    /**
     * 获取汇率
     */
    @GetMapping("/rate")
    public Result<BigDecimal> getRate(@RequestParam String from, @RequestParam String to) {
        log.info("控制器 - 获取汇率: from={}, to={}", from, to);
        
        try {
            BigDecimal rate = currencyAppService.getExchangeRate(from, to);
            log.info("控制器 - 获取汇率成功: from={}, to={}, rate={}", from, to, rate);
            return Result.success(rate);
        } catch (Exception e) {
            log.error("控制器 - 获取汇率失败: from={}, to={}, error={}", from, to, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 货币转换
     */
    @PostMapping("/convert")
    public Result<Map<String, Object>> convert(@RequestBody Map<String, Object> request) {
        String from = (String) request.get("from");
        String to = (String) request.get("to");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        
        log.info("控制器 - 货币转换: from={}, to={}, amount={}", from, to, amount);

        try {
            BigDecimal result = currencyAppService.convert(from, to, amount);
            BigDecimal rate = currencyAppService.getExchangeRate(from, to);

            Map<String, Object> data = new HashMap<>();
            data.put("fromCurrency", from);
            data.put("toCurrency", to);
            data.put("amount", amount);
            data.put("result", result);
            data.put("rate", rate);

            log.info("控制器 - 货币转换成功: from={}, to={}, amount={}, result={}", from, to, amount, result);
            return Result.success(data);
        } catch (Exception e) {
            log.error("控制器 - 货币转换失败: from={}, to={}, amount={}, error={}", from, to, amount, e.getMessage(), e);
            throw e;
        }
    }

}