package com.macau.bank.transfer.application.service;

import com.macau.bank.transfer.application.command.TransferCmd;
import com.macau.bank.transfer.application.result.TransferOrderResult;
import com.macau.bank.transfer.application.result.TransferResult;

import java.util.List;

/**
 * 转账应用服务
 * <p>
 * 职责：编排转账业务流程，协调领域服务和策略执行
 */
public interface TransferAppService {

    /**
     * 发起转账
     *
     * @param cmd 转账命令对象
     * @return 转账结果（受理中/成功/失败）
     */
    TransferResult submitTransfer(TransferCmd cmd);

    /**
     * 分页查询转账订单列表
     *
     * @param payerAccountId     付款账户ID（可选）
     * @param payeeAccountNumber 收款账户号（可选）
     * @param page               页码
     * @param pageSize           每页数量
     * @return 转账订单结果列表
     */
    List<TransferOrderResult> getTransferOrders(Long payerAccountId, String payeeAccountNumber, Integer page,
            Integer pageSize);

    /**
     * 根据ID查询转账订单详情
     *
     * @param id 订单ID
     * @return 转账订单结果，不存在时返回 null
     */
    TransferOrderResult getTransferOrderById(Long id);

    /**
     * 执行每日对账任务
     *
     * @param shardIndex 分片索引（用于分布式任务调度）
     */
    void executeDailyReconciliation(int shardIndex);

    /**
     * 冲正/退款转账订单
     * <p>
     * 用于处理已成功或失败的转账订单的逆向操作：
     * - 成功订单：从收款方扣回资金，退回付款方
     * - 失败订单：解冻被冻结的资金
     *
     * @param orderId        订单ID
     * @param reversalReason 冲正原因
     * @return 冲正后的订单信息
     */
    TransferOrderResult reverseOrder(Long orderId, String reversalReason);
}