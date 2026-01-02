package com.macau.bank.transfer.domain.event; // 建议放在 Domain 层

import com.macau.bank.transfer.domain.context.TransferContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 转账事务预备事件
 * 含义：转账校验已通过，准备发起分布式事务
 */
@Getter
public class TransferTransactionPreparedEvent extends ApplicationEvent {

    /**
     * 携带转账上下文，供监听器使用
     */
    private final TransferContext context;

    public TransferTransactionPreparedEvent(Object source, TransferContext context) {
        super(source);
        this.context = context;
    }
}