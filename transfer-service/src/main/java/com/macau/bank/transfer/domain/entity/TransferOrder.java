package com.macau.bank.transfer.domain.entity;

import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.*;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.transfer.domain.valobj.PayeeInfo;
import com.macau.bank.transfer.domain.valobj.PayerInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 转账订单领域实体（聚合根）
 * <p>
 * 核心业务对象，承载转账业务规则与状态
 * <p>
 * 领域行为：
 * - 状态流转保护（transitionTo）
 * - 标记成功/失败
 * - 冲正判断
 */
@Getter
@Setter
@ToString
public class TransferOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 本行交易流水号 (全局唯一)
     */
    private String txnId;

    /**
     * 外部通道/清算返回流水号
     */
    private String externalTxnId;

    /**
     * 幂等键 (防重提交)
     */
    /**
     * 幂等键 (防重提交)
     */
    private String idempotentKey;

    /**
     * 付款方信息（值对象）
     */
    private PayerInfo payerInfo;

    /**
     * 收款方信息（值对象）
     */
    private PayeeInfo payeeInfo;

    /**
     * 交易金额（值对象）
     */
    private Money amount;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 手续费承担方式: SHA/OUR/BEN
     */
    private FeeType feeType;

    /**
     * 转账类型: NORMAL/CROSS_BORDER/FPS/SWIFT
     */
    private TransferType transferType;

    /**
     * 转账通道: INTERNAL/SWIFT/FPS/CIPS/LOCAL_CLEARING
     */
    private TransferChannel transferChannel;

    /**
     * 跨境用途代码
     */
    private String purposeCode;

    /**
     * 外部参考号 (如 MT103 ID)
     */
    private String referenceNo;

    /**
     * 交易备注/附言
     */
    private String remark;

    /**
     * 状态: PROCESSING/SUCCESS/FAILED
     */
    private TransferStatus status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 反洗钱状态: PENDING/PASSED/REJECTED/MANUAL_REVIEW/REPORTED
     */
    private AmlStatus amlStatus;

    /**
     * 反洗钱详情
     */
    private String amlDetail;

    /**
     * 风控状态: PENDING/PASSED/REJECTED/MANUAL_REVIEW
     */
    private RiskStatus riskStatus;

    /**
     * 风控详情
     */
    private String riskDetail;

    /**
     * 扩展字段 (JSON)
     */
    private String extendInfo;

    private Deleted deleted;

    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ==================== 领域行为 ====================

    /**
     * 状态流转（带状态机保护）
     * <p>
     * 只有合法的状态转换才能执行，否则抛出业务异常
     *
     * @param newStatus 目标状态
     * @throws BusinessException 如果状态转换非法
     */
    public void transitionTo(TransferStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new BusinessException(
                    String.format("非法状态流转: %s -> %s, txnId=%s", status, newStatus, txnId));
        }
        this.status = newStatus;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记订单成功
     *
     * @param externalTxnId 外部交易流水号（可选）
     */
    public void markSuccess(String externalTxnId) {
        transitionTo(TransferStatus.SUCCESS);
        this.externalTxnId = externalTxnId;
    }

    /**
     * 标记订单成功（无外部流水号）
     */
    public void markSuccess() {
        transitionTo(TransferStatus.SUCCESS);
    }

    /**
     * 标记订单失败
     *
     * @param reason 失败原因
     */
    public void markFailed(String reason) {
        transitionTo(TransferStatus.FAILED);
        this.failReason = reason;
    }

    /**
     * 判断订单是否可冲正
     * <p>
     * 只有成功或待审核中的订单可以发起冲正
     *
     * @return true=可冲正，false=不可冲正
     */
    public boolean canReverse() {
        return status == TransferStatus.SUCCESS
                || status == TransferStatus.PENDING_RISK;
    }

    /**
     * 判断订单是否已完结
     * <p>
     * 成功或失败状态为完结状态
     *
     * @return true=已完结，false=未完结
     */
    public boolean isTerminal() {
        return status == TransferStatus.SUCCESS
                || status == TransferStatus.FAILED
                || status == TransferStatus.REVERSED;
    }

    /**
     * 判断订单金额是否有效
     *
     * @return true=金额大于0，false=金额无效
     */
    public boolean hasValidAmount() {
        return amount != null && amount.isPositive();
    }

    /**
     * 状态流转规则
     * <p>
     * 定义合法的状态转换路径
     * 基于实际 TransferStatus 枚举：INIT, PENDING_RISK, SUCCESS, FAILED,
     * PENDING_COMPENSATION, REVERSING, REVERSED, REFUNDED
     */
    private boolean canTransitionTo(TransferStatus newStatus) {
        if (status == null) {
            // 初始化时可以设置任意状态
            return true;
        }

        return switch (status) {
            case INIT -> Set.of(
                    TransferStatus.PENDING_RISK,
                    TransferStatus.SUCCESS,
                    TransferStatus.FAILED).contains(newStatus);

            case PENDING_RISK -> Set.of(
                    TransferStatus.SUCCESS,
                    TransferStatus.FAILED,
                    TransferStatus.PENDING_COMPENSATION).contains(newStatus);

            case PENDING_COMPENSATION -> Set.of(
                    TransferStatus.SUCCESS,
                    TransferStatus.FAILED).contains(newStatus);

            case SUCCESS -> Set.of(
                    TransferStatus.REVERSING,
                    TransferStatus.REFUNDED).contains(newStatus);

            case REVERSING -> Set.of(
                    TransferStatus.REVERSED,
                    TransferStatus.FAILED).contains(newStatus);

            // 终态不可再流转
            case FAILED, REVERSED, REFUNDED -> false;
        };
    }
}
