package com.macau.bank.transfer.domain.ability;

import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.service.PayeeDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 转账后置处理器 (善后专员)
 * 职责：负责事务提交后的异步操作（发消息、更新联系人）
 */
@Slf4j
@Component
public class TransferPostProcessor {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private PayeeDomainService payeeDomainService;

    /**
     * 核心方法：注册事务同步回调
     * 只有当当前数据库事务【成功提交】后，才会执行里面的逻辑
     */
    public void processAfterCommit(TransferContext context, String txnId) {

        // 判断当前是否存在事务，如果存在，则注册回调
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // --- 这里是事务提交后执行的，安全区 ---
                    log.info("主事务提交成功，开始执行后置处理: txnId={}", txnId);
                    doAsyncTasks(context, txnId);
                }
            });
        } else {
            // 如果当前没事务（比如测试环境直接调），直接执行
            log.warn("当前无事务环境，直接执行后置处理");
            doAsyncTasks(context, txnId);
        }
    }

    /**
     * 执行具体的异步任务
     * 这里面的异常绝对不能抛出去影响主流程，必须 try-catch 吃掉
     */
    private void doAsyncTasks(TransferContext context, String txnId) {
        try {
            // 2. 更新常用收款人 (锦上添花的功能，失败了也就失败了)
            updatePayeeHistory(context);
        } catch (Exception e) {
            log.warn("后置处理异常-更新联系人失败: txnId={}", txnId);
        }
    }

    private void updatePayeeHistory(TransferContext context) {
        // 如果有收款人账号，尝试更新最近联系人
        TransferOrder order = context.getOrder();
        if (order.getPayeeInfo() != null && order.getPayeeInfo().getAccountNo() != null) {
            payeeDomainService.updatePayeeHistory(
                    order.getPayerInfo().getUserNo(),
                    order.getPayeeInfo().getAccountName(),
                    order.getPayeeInfo().getAccountNo(),
                    order.getAmount().getCurrencyCode());
        }
    }
}