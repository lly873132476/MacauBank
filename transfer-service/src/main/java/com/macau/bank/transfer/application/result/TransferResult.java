package com.macau.bank.transfer.application.result;

import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.transfer.domain.context.TransferContext;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransferResult {
    private String txnId;       // 交易流水号 (凭证)
    private TransferStatus status;      // 状态
    private String message;     // 提示语
    private LocalDateTime createTime;     // 提示语

    /**
     * 静态工厂方法：快速生成一个“处理中”的返回结果
     */
    public static TransferResult processing(TransferContext context) {
        return TransferResult.builder()
                .txnId(context.getOrder().getTxnId())
                .status(context.getOrder().getStatus())
                .message(context.getOrder().getStatus().getDesc())
                .build();
    }

    /**
     * 静态工厂方法：快速生成一个“成功”的返回结果 (同步场景用)
     */
    public static TransferResult success(String txnId) {
        return TransferResult.builder()
                .txnId(txnId)
                .status(TransferStatus.SUCCESS)
                .message("转账成功")
                .build();
    }
}