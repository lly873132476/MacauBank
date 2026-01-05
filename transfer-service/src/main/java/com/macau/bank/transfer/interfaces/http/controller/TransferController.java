package com.macau.bank.transfer.interfaces.http.controller;

import com.macau.bank.api.dto.TransferRequest;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.transfer.application.command.TransferCmd;
import com.macau.bank.transfer.common.annotation.PreventDuplicate;
import com.macau.bank.transfer.interfaces.http.assembler.TransferWebAssembler;
import com.macau.bank.transfer.application.result.TransferOrderResult;
import com.macau.bank.transfer.application.result.TransferResult;
import com.macau.bank.transfer.application.service.TransferAppService;
import com.macau.bank.transfer.interfaces.http.response.TransferOrderResponse;
import com.macau.bank.transfer.interfaces.http.response.TransferResponse;
import com.macau.bank.transfer.interfaces.http.request.TransferReversalRequest;
import org.springframework.web.bind.annotation.*;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 转账服务控制器
 */
@RestController
@RequestMapping("/transfer")
@Validated
public class TransferController {

    @Resource
    private TransferAppService transferAppService;

    @Resource
    private TransferWebAssembler transferWebAssembler;

    /**
     * 发起转账
     */
    @PreventDuplicate(prefix = "transfer:submit:", timeout = 5, message = "请勿重复提交转账请求")
    @PostMapping("/submit")
    public Result<TransferResponse> submitTransfer(@RequestBody @Valid TransferRequest request) {
        // 1. 【拆包】 Request -> Cmd
        TransferCmd cmd = transferWebAssembler.toCmd(request);

        // 2. 【办事】 Call App Service
        TransferResult result = transferAppService.submitTransfer(cmd);

        // 3. 【包装】 Result -> Response
        return Result.success(transferWebAssembler.toResponse(result));
    }

    /**
     * 查询转账记录
     */
    @GetMapping("/list")
    public Result<List<TransferOrderResponse>> getTransferOrders(
            @RequestParam(required = false) Long payerAccountId,
            @RequestParam(required = false) String payeeAccountNumber,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        List<TransferOrderResult> records = transferAppService.getTransferOrders(
                payerAccountId, payeeAccountNumber, page, pageSize);
        return Result.success(transferWebAssembler.toResponseList(records));
    }

    /**
     * 查询转账详情
     */
    @GetMapping("/{id}")
    public Result<TransferOrderResponse> getTransferOrderById(
            @PathVariable Long id) {

        TransferOrderResult record = transferAppService.getTransferOrderById(id);
        return Result.success(transferWebAssembler.toResponse(record));
    }

    @GetMapping("/test-sentinel")
    public String testSentinel() {
        return "转账服务正常运行中... " + System.currentTimeMillis();
    }

    /**
     * 冲正/退款转账订单
     * <p>
     * 用于处理已成功或失败的转账订单的逆向操作：
     * - 成功订单：从收款方扣回资金，退回付款方
     * - 失败订单：解冻被冻结的资金
     */
    @PostMapping("/reverse")
    public Result<TransferOrderResponse> reverseOrder(
            @RequestBody @Valid TransferReversalRequest request) {
        // 1. 【拆包】 提取参数
        Long orderId = request.getOrderId();
        String reversalReason = request.getReversalReason();

        // 2. 【办事】 Call App Service
        TransferOrderResult result = transferAppService.reverseOrder(orderId, reversalReason);

        // 3. 【包装】 Result -> Response
        return Result.success(transferWebAssembler.toResponse(result));
    }
}