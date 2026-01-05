package com.macau.bank.transfer.application.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.transfer.application.command.TransferCmd;
import com.macau.bank.transfer.application.fallback.TransferSentinelFallback;
import com.macau.bank.transfer.common.annotation.Auditable;
import com.macau.bank.transfer.application.result.TransferOrderResult;
import com.macau.bank.transfer.application.result.TransferResult;
import com.macau.bank.transfer.application.service.TransferAppService;
import com.macau.bank.transfer.domain.ability.TransferContextBuilder;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.factory.TransferStrategyFactory;
import com.macau.bank.transfer.domain.service.TransferOrderDomainService;
import com.macau.bank.transfer.domain.service.TransferReversalDomainService;
import com.macau.bank.transfer.domain.strategy.TransferStrategy;
import com.macau.bank.transfer.interfaces.assembler.TransferDtoAssembler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 转账应用服务实现
 */
@Slf4j
@Service
public class TransferAppServiceImpl implements TransferAppService {

    @Resource
    private TransferStrategyFactory transferStrategyFactory;

    @Resource
    private TransferOrderDomainService transferOrderDomainService;

    @Resource
    private TransferDtoAssembler transferDtoAssembler;

    @Resource
    private TransferContextBuilder transferContextBuilder;

    @Override
    @SentinelResource(value = "transfer:submit", blockHandlerClass = TransferSentinelFallback.class, blockHandler = "submitTransferBlockHandler", fallbackClass = TransferSentinelFallback.class, fallback = "submitTransferFallback", exceptionsToIgnore = {
            BusinessException.class })
    @Auditable(action = "TRANSFER_SUBMIT", targetType = "TRANSFER_ORDER", targetIdExpr = "#cmd.idempotentKey")
    public TransferResult submitTransfer(TransferCmd cmd) {
        log.info("应用服务 - 接收转账指令: from={}, to={}, amount={}",
                cmd.getFromAccountNo(), cmd.getToAccountNo(), cmd.getAmount());

        // 1. 根据转账类型获取策略
        TransferStrategy strategy = transferStrategyFactory.getStrategy(cmd.getTransferType());

        // 2. 构建上下文 (Cmd -> Context)
        TransferContext context = transferContextBuilder.build(cmd);

        // 3. 执行转账
        return strategy.execute(context);
    }

    @Override
    public List<TransferOrderResult> getTransferOrders(Long payerAccountId, String payeeAccountNumber,
            Integer page, Integer pageSize) {
        // 构建查询条件
        TransferOrder condition = new TransferOrder();
        condition.setPayeeAccountNo(payeeAccountNumber);

        List<TransferOrder> records = transferOrderDomainService.getTransferOrders(condition, page, pageSize);

        return transferDtoAssembler.toResultList(records);
    }

    @Override
    public TransferOrderResult getTransferOrderById(Long id) {
        TransferOrder record = transferOrderDomainService.getTransferOrderById(id);
        if (record == null) {
            return null;
        }
        return transferDtoAssembler.toResult(record);
    }

    @Override
    public void executeDailyReconciliation(int shardIndex) {
        // 这里是编排逻辑：
        // 1. 调用 Gateway 获取渠道账单
        // 2. 调用 Domain Service 进行核心比对
        // 3. 发送消息通知
        log.info("应用层正在编排对账流程，分片：{}", shardIndex);
    }

    @Resource
    private TransferReversalDomainService transferReversalDomainService;

    @Override
    @Auditable(action = "TRANSFER_REVERSE", targetType = "TRANSFER_ORDER", targetIdExpr = "#orderId")
    public TransferOrderResult reverseOrder(Long orderId, String reversalReason) {
        log.info("应用服务 - 接收冲正请求: orderId={}, reason={}", orderId, reversalReason);

        // 调用领域服务执行冲正
        TransferOrder reversedOrder = transferReversalDomainService.reverseOrder(orderId, reversalReason);

        // 转换为结果对象
        return transferDtoAssembler.toResult(reversedOrder);
    }
}