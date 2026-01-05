package com.macau.bank.transfer.common.result;

import com.macau.bank.common.core.result.IResultCode;
import lombok.Getter;

/**
 * Transfer服务错误码
 */
@Getter
public enum TransferErrorCode implements IResultCode {

    // ==================== Transfer服务 - 业务错误 (3030xx) ====================
    TRANSFER_FAILED(303001, "转账失败", "transfer.failed"),
    PAYEE_NOT_FOUND(303002, "收款人不存在", "transfer.payee.not.found"),
    PAYEE_ALREADY_EXISTS(303003, "收款人已存在", "transfer.payee.already.exists"),
    PAYEE_PERMISSION_DENIED(303004, "收款人授权被拒绝", "transfer.payee.permission.denied"),

    TRANSFER_AMOUNT_INVALID(303005, "转账金额无效", "transfer.amount.invalid"),

    FROM_ACCOUNT_NOT_FOUND(303006, "付款账户不存在", "transfer.from.account.not.found"),
    FROM_ACCOUNT_STATUS_ERROR(303007, "付款账户状态异常", "transfer.from.account.status.error"),
    FROM_ACCOUNT_BALANCE_NOT_ENOUGH(303008, "付款账户余额不足", "transfer.from.account.balance.not.enough"),
    FROM_ACCOUNT_BALANCE_EXCEED_LIMIT(303009, "付款账户余额超出限额", "transfer.from.account.balance.exceed.limit"),

    TO_ACCOUNT_NOT_FOUND(303010, "收款账户不存在", "transfer.to.account.not.found"),
    TO_ACCOUNT_NOT_NULL(303011, "收款账号不能为空", "transfer.to.account.not.null"),

    TRANSFER_AMOUNT_EXCEED_LIMIT(303012, "超出单笔转账限额", "transfer.amount.exceed.limit"),

    DUPLICATE_REQUEST(303013, "请勿重复提交", "transfer.duplicate.request"),

    // ==================== Transfer服务 - 系统错误 (3039xx) ====================
    SYSTEM_BUSY(303901, "系统繁忙，请稍后重试", "transfer.system.busy"),
    SYSTEM_ERROR(303902, "系统异常，请稍后重试", "transfer.system.error"),

    // ==================== Transfer服务 - 冲正错误 (3038xx) ====================
    REVERSAL_NOT_ALLOWED(303801, "订单状态不允许冲正", "transfer.reversal.not.allowed"),
    REVERSAL_ALREADY_DONE(303802, "订单已冲正，请勿重复操作", "transfer.reversal.already.done"),
    REVERSAL_IN_PROGRESS(303803, "订单正在冲正中", "transfer.reversal.in.progress"),
    ORDER_NOT_FOUND(303804, "订单不存在", "transfer.order.not.found"),

    // ==================== Transfer服务 - 并发控制 (3037xx) ====================
    CONCURRENCY_CONFLICT(303701, "订单并发更新冲突，请重试", "transfer.concurrency.conflict");

    private final Integer code;
    private final String message;
    private final String i18nKey;

    TransferErrorCode(Integer code, String message, String i18nKey) {
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }
}