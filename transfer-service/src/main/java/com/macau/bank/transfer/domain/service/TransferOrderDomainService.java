package com.macau.bank.transfer.domain.service;

import com.macau.bank.common.core.enums.Deleted;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.util.IdGenerator;
import com.macau.bank.transfer.common.result.TransferErrorCode;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.query.TransferOrderQuery;
import com.macau.bank.transfer.domain.repository.TransferOrderRepository;
import com.macau.bank.transfer.domain.query.TransferOrderQuery;
import com.macau.bank.transfer.domain.valobj.PayerInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 转账订单领域服务
 * <p>
 * 职责：
 * - 生成交易流水号
 * - 协调订单持久化
 * - 提供查询接口
 * <p>
 * 状态变更逻辑已内聚到 {@link TransferOrder} 实体
 */
@Slf4j
@Service
public class TransferOrderDomainService {

    @Resource
    private TransferOrderRepository transferOrderRepository;

    /**
     * 创建转账订单
     *
     * @param context 转账上下文，包含订单信息和账户快照
     * @return 生成的交易流水号（txnId）
     */
    public String createOrder(TransferContext context) {
        TransferOrder order = context.getOrder();
        order.setTxnId("TR" + IdGenerator.generateId());

        // 填充付款方 (从 ValObj 映射)
        order.setPayerInfo(PayerInfo.fromSnapshot(
                context.getPayerAccount().getUserNo(),
                context.getPayerAccount().getAccountNo(),
                context.getPayerAccount().getAccountName(),
                context.getPayerAccount().getCurrencyCode()));

        // 填充交易信息（使用实体的初始化逻辑）
        order.setStatus(TransferStatus.INIT);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(Deleted.NO_DELETED);
        order.setVersion(0);

        transferOrderRepository.save(order);

        context.setOrder(order);
        return order.getTxnId();
    }

    /**
     * 更新订单状态（使用实体的状态流转保护）
     *
     * @param txnId  交易流水号
     * @param status 新状态
     */
    public void updateStatus(String txnId, TransferStatus status) {
        TransferOrder order = transferOrderRepository.findByTxnId(txnId);
        if (order == null) {
            log.warn("订单不存在: txnId={}", txnId);
            return;
        }

        // 使用实体的状态流转方法（带状态机保护）
        order.transitionTo(status);
        transferOrderRepository.save(order);
    }

    /**
     * 标记订单成功
     *
     * @param txnId         交易流水号
     * @param externalTxnId 外部交易流水号（可选）
     */
    public void markSuccess(String txnId, String externalTxnId) {
        TransferOrder order = transferOrderRepository.findByTxnId(txnId);
        if (order == null) {
            throw new BusinessException("订单不存在: " + txnId);
        }

        // 使用实体的充血方法
        order.markSuccess(externalTxnId);
        transferOrderRepository.save(order);
    }

    /**
     * 标记订单失败
     *
     * @param txnId      交易流水号
     * @param failReason 失败原因
     */
    public void markFailed(String txnId, String failReason) {
        TransferOrder order = transferOrderRepository.findByTxnId(txnId);
        if (order == null) {
            throw new BusinessException("订单不存在: " + txnId);
        }

        // 使用实体的充血方法
        order.markFailed(failReason);
        transferOrderRepository.save(order);
    }

    /**
     * 根据主键ID查询转账订单
     */
    public TransferOrder getTransferOrderById(Long id) {
        return transferOrderRepository.findById(id);
    }

    /**
     * 根据交易流水号查询转账订单
     */
    public TransferOrder getTransferOrderByTxnId(String txnId) {
        return transferOrderRepository.findByTxnId(txnId);
    }

    /**
     * 查询转账订单列表
     */
    public List<TransferOrder> getTransferOrders(TransferOrderQuery condition, Integer page, Integer pageSize) {
        return transferOrderRepository.query(condition);
    }

    /**
     * 检查订单是否已成功
     */
    public boolean checkOrderStatus(String txnId) {
        TransferOrder order = transferOrderRepository.findByTxnId(txnId);
        return order != null && order.isTerminal()
                && TransferStatus.SUCCESS.equals(order.getStatus());
    }

    /**
     * 保存订单（带乐观锁控制）
     */
    public void saveOrder(TransferOrder order) {
        boolean success = transferOrderRepository.save(order);
        if (!success) {
            log.error("订单更新失败,可能是并发冲突: txnId={}, version={}",
                    order.getTxnId(), order.getVersion());
            throw new BusinessException(TransferErrorCode.CONCURRENCY_CONFLICT);
        }
    }
}
