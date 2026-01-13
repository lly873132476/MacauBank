package com.macau.bank.transfer.domain.valobj;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 付款方信息值对象（不可变）
 * <p>
 * 封装付款方账户相关信息
 */
@Builder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public final class PayerInfo {

    /**
     * 用户编号
     */
    private final String userNo;

    /**
     * 账户号码
     */
    private final String accountNo;

    /**
     * 账户名称
     */
    private final String accountName;

    /**
     * 账户币种
     */
    private final String currency;

    private PayerInfo(String userNo, String accountNo, String accountName, String currency) {
        this.userNo = userNo;
        this.accountNo = accountNo;
        this.accountName = accountName;
        this.currency = currency;
    }

    /**
     * 工厂方法：创建付款方信息
     */
    public static PayerInfo of(String userNo, String accountNo, String accountName, String currency) {
        return new PayerInfo(userNo, accountNo, accountName, currency);
    }

    /**
     * 从账户快照创建
     */
    public static PayerInfo fromSnapshot(String userNo, String accountNo,
            String accountName, String currency) {
        return new PayerInfo(userNo, accountNo, accountName, currency);
    }
}
