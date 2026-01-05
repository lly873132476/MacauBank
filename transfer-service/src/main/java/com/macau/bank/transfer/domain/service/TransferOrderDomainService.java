package com.macau.bank.transfer.domain.service;

import com.macau.bank.common.core.enums.Deleted;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.common.core.util.IdGenerator;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.repository.TransferOrderRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 转账订单领域服务 - 纯净领域层
 */
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
        order.setPayerAccountNo(context.getPayerAccount().getAccountNo());
        order.setPayerAccountName(context.getPayerAccount().getAccountName());

        // 填充交易信息
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
     * 更新订单状态
     *
     * @param txnId  交易流水号
     * @param status 新状态
     */
    public void updateStatus(String txnId, TransferStatus status) {
        updateStatus(txnId, status, null);
    }

    /**
     * 更新订单状态（带失败原因）
     *
     * @param txnId      交易流水号
     * @param status     新状态
     * @param failReason 失败原因（可选）
     */
    public void updateStatus(String txnId, TransferStatus status, String failReason) {
        TransferOrder order = transferOrderRepository.findByTxnId(txnId);
        if (order != null) {
            order.setStatus(status);
            order.setFailReason(failReason);
            order.setUpdateTime(LocalDateTime.now());
            transferOrderRepository.save(order);
        }
    }

    /**
     * 根据主键ID查询转账订单
     *
     * @param id 订单主键ID
     * @return 转账订单实体，不存在时返回 null
     */
    public TransferOrder getTransferOrderById(Long id) {
        return transferOrderRepository.findById(id);
    }

    /**
     * 根据交易流水号查询转账订单
     *
     * @param txnId 交易流水号
     * @return 转账订单实体，不存在时返回 null
     */
    public TransferOrder getTransferOrderByTxnId(String txnId) {
        return transferOrderRepository.findByTxnId(txnId);
    }

    /**
     * 分页查询转账订单列表
     *
     * @param condition 查询条件（可包含收款账号等过滤条件）
     * @param page      页码
     * @param pageSize  每页数量
     * @return 转账订单列表
     */
    public List<TransferOrder> getTransferOrders(TransferOrder condition, Integer page, Integer pageSize) {
        // 分页逻辑暂由Controller/AppService处理或在Repository中增加Page支持
        // 此处简化为直接查询列表
        return transferOrderRepository.query(condition);
    }

    /**
     * 检查订单是否已成功
     *
     * @param txnId 交易流水号
     * @return true=订单存在且状态为成功，false=订单不存在或状态非成功
     */
    public boolean checkOrderStatus(String txnId) {
        TransferOrder order = transferOrderRepository.findByTxnId(txnId);
        return order != null && TransferStatus.SUCCESS.equals(order.getStatus());
    }

    /**
     * 根据ID更新订单，仅更新非空字段
     *
     * @param order 订单
     */
    public void updateOrder(TransferOrder order) {
        transferOrderRepository.save(order);
    }
}
