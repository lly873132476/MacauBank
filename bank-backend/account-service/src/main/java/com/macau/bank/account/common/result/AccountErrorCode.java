package com.macau.bank.account.common.result;

import com.macau.bank.common.core.result.IResultCode;
import lombok.Getter;

/**
 * Account服务错误码
 */
@Getter
public enum AccountErrorCode implements IResultCode {

    // ==================== Account服务 - 业务错误 (2030xx) ====================
    ACCOUNT_NOT_FOUND(203001, "账户不存在", "account.not.found"),
    BALANCE_INSUFFICIENT(203002, "余额不足", "account.balance.insufficient"),
    ACCOUNT_FROZEN(203003, "账户已冻结", "account.frozen"),
    CURRENCY_NOT_SUPPORTED(203004, "不支持的货币类型", "account.currency.not.supported"),
    BALANCE_RECORD_NOT_FOUND(203005, "余额记录不存在", "account.balance.not.found"),
    INVALID_OPERATION(203006, "非法操作", "account.invalid.operation"),
    CONCURRENCY_CONFLICT(203007, "并发更新冲突", "account.concurrency.conflict"),
    USER_CONTEXT_MISSING(203008, "用户上下文缺失，请重新登录", "account.user.context.missing"),
    ACCOUNT_NOT_BELONG_TO_USER(203009, "账户不属于当前用户", "account.not.belong.to.user"),
    DUPLICATE_REQUEST(203010, "重复请求，请勿重复提交", "account.duplicate.request"),
    INVALID_AMOUNT(203011, "金额必须为正数", "account.invalid.amount"),
    FROZEN_BALANCE_NOT_ENOUGH(203012, "冻结金额不足", "account.frozen.balance.not.enough");

    private final Integer code;
    private final String message;
    private final String i18nKey;

    AccountErrorCode(Integer code, String message, String i18nKey) {
        this.code = code;
        this.message = message;
        this.i18nKey = i18nKey;
    }
}