package com.macau.bank.transfer.application.result;

import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.transfer.domain.context.TransferContext;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 转账执行结果（Application 层 DTO）
 * <p>
 * 职责：作为应用服务的返回值，封装转账执行的结果信息
 * 位置：Application 层（符合 DDD 分层规范）
 */
@Data
@Builder
public class TransferResult {
    /**
     * 交易流水号（凭证）
     */
    private String txnId;

    /**
     * 状态
     */
    private TransferStatus status;

    /**
     * 提示语
     */
    private String message;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 静态工厂方法：从 TransferContext 组装"处理中"结果
     * <p>
     * 由 Application 层调用，Domain 层执行完成后组装返回值
     */
    public static TransferResult fromContext(TransferContext context) {
        return TransferResult.builder()
                .txnId(context.getOrder().getTxnId())
                .status(context.getOrder().getStatus())
                .message(context.getOrder().getStatus().getDesc())
                .createTime(context.getOrder().getCreateTime())
                .build();
    }

    /**
     * 静态工厂方法：快速生成一个"成功"的返回结果 (同步场景用)
     */
    public static TransferResult success(String txnId) {
        return TransferResult.builder()
                .txnId(txnId)
                .status(TransferStatus.SUCCESS)
                .message("转账成功")
                .build();
    }
}
