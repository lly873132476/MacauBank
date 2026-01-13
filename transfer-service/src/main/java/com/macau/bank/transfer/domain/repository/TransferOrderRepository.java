package com.macau.bank.transfer.domain.repository;

import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.query.TransferOrderQuery;
import java.util.List;

/**
 * 转账订单仓储接口
 * <p>
 * DDD 战术设计：Repository 属于 Domain 层，定义领域对象的存储与重建标准
 */
public interface TransferOrderRepository {

    /**
     * 保存或更新转账订单（通过id）
     *
     * @param transferOrder 领域实体
     * @return 是否成功
     */
    boolean save(TransferOrder transferOrder);

    /**
     * 根据ID查询
     *
     * @param id 主键
     * @return 领域实体
     */
    TransferOrder findById(Long id);

    /**
     * 根据交易流水号查询
     *
     * @param txnId 本行流水号
     * @return 领域实体
     */
    TransferOrder findByTxnId(String txnId);

    /**
     * 列表查询
     *
     * @param condition 查询条件
     * @return 结果列表
     */
    List<TransferOrder> query(TransferOrderQuery condition);

}
