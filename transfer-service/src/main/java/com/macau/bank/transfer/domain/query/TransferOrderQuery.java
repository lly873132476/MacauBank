package com.macau.bank.transfer.domain.query;

import com.macau.bank.common.core.enums.TransferStatus;
import lombok.Builder;
import lombok.Data;

/**
 * 转账订单查询条件 VO
 * <p>
 * 用于解耦 Domain Entity 与查询条件
 */
@Data
@Builder
public class TransferOrderQuery {

    /**
     * 付款方用户编号
     */
    private String payerUserNo;

    /**
     * 收款方账号
     */
    private String payeeAccountNo;

    /**
     * 转账状态
     */
    private TransferStatus status;

}
