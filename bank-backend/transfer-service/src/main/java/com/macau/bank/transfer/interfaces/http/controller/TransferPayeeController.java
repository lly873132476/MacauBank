package com.macau.bank.transfer.interfaces.http.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.common.framework.web.context.RequestHeaderContext;
import com.macau.bank.transfer.application.command.AddPayeeCmd;
import com.macau.bank.transfer.application.command.UpdatePayeeCmd;
import com.macau.bank.transfer.application.result.PayeeResult;
import com.macau.bank.transfer.application.service.PayeeAppService;
import com.macau.bank.transfer.interfaces.http.assembler.TransferWebAssembler;
import com.macau.bank.transfer.interfaces.http.request.AddPayeeRequest;
import com.macau.bank.transfer.interfaces.http.request.UpdatePayeeRequest;
import com.macau.bank.transfer.interfaces.http.response.PayeeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 转账收款人控制器
 */
@RestController
@RequestMapping("/transfer/payee")
@Tag(name = "TransferPayee", description = "转账收款人管理")
@Validated
@Slf4j
public class TransferPayeeController {

    @Resource
    private PayeeAppService payeeAppService;

    @Resource
    private TransferWebAssembler transferWebAssembler;

    @GetMapping("/page")
    @Operation(summary = "分页查询收款人")
    public Result<Page<PayeeResponse>> getPayeePage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) throws InterruptedException {

        // 1. 【拆包】
        String userNo = RequestHeaderContext.get().getUserNo();

        // 2. 【办事】
        Page<PayeeResult> pageResult = payeeAppService.getPayeePage(userNo, current, size);

        // 3. 【包装】 Result -> Response
        Page<PayeeResponse> responsePage = new Page<>(pageResult.getCurrent(), pageResult.getSize(),
                pageResult.getTotal());
        responsePage.setRecords(transferWebAssembler.toPayeeResponseList(pageResult.getRecords()));

        return Result.success(responsePage);
    }

    @PostMapping("/add")
    @Operation(summary = "添加收款人")
    public Result<Void> addPayee(@RequestBody @Validated AddPayeeRequest request) {
        // 1. 【拆包】 Request -> Cmd
        AddPayeeCmd cmd = transferWebAssembler.toCmd(request);
        cmd.setUserNo(RequestHeaderContext.get().getUserNo());

        // 2. 【办事】 Call App Service
        payeeAppService.addPayee(cmd);

        // 3. 【包装】 Result -> Response
        return Result.success();
    }

    @PostMapping("/update")
    @Operation(summary = "更新收款人")
    public Result<Void> updatePayee(@RequestBody @Validated UpdatePayeeRequest request) {
        // 1. 【拆包】 Request -> Cmd
        UpdatePayeeCmd cmd = transferWebAssembler.toCmd(request);
        cmd.setUserNo(RequestHeaderContext.get().getUserNo());

        // 2. 【办事】 Call App Service
        payeeAppService.updatePayee(cmd);

        // 3. 【包装】 Result -> Response
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除收款人")
    public Result<Void> deletePayee(@PathVariable Long id) {
        // 1. 【拆包】
        String userNo = RequestHeaderContext.get().getUserNo();

        // 2. 【办事】
        payeeAppService.deletePayee(userNo, id);

        // 3. 【包装】 Result -> Response
        return Result.success();
    }

}
