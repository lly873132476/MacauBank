package com.macau.bank.transfer.domain.valobj;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 收款方信息值对象（不可变）
 * <p>
 * 封装收款方账户相关信息，支持多种收款渠道
 */
@Getter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public final class PayeeInfo {

    /**
     * 账户号码
     */
    private final String accountNo;

    /**
     * 账户名称
     */
    private final String accountName;

    /**
     * 银行代码（本地清算）
     */
    private final String bankCode;

    /**
     * SWIFT 代码（跨境汇款）
     */
    private final String swiftCode;

    /**
     * FPS ID（转数快）
     */
    private final String fpsId;

    /**
     * 工厂方法：本地清算收款方
     */
    public static PayeeInfo forLocalClearing(String accountNo, String accountName, String bankCode) {
        return PayeeInfo.builder()
                .accountNo(accountNo)
                .accountName(accountName)
                .bankCode(bankCode)
                .build();
    }

    /**
     * 工厂方法：SWIFT 跨境收款方
     */
    public static PayeeInfo forSwift(String accountNo, String accountName,
            String bankCode, String swiftCode) {
        return PayeeInfo.builder()
                .accountNo(accountNo)
                .accountName(accountName)
                .bankCode(bankCode)
                .swiftCode(swiftCode)
                .build();
    }

    /**
     * 工厂方法：FPS 转数快收款方
     */
    public static PayeeInfo forFps(String accountNo, String accountName, String fpsId) {
        return PayeeInfo.builder()
                .accountNo(accountNo)
                .accountName(accountName)
                .fpsId(fpsId)
                .build();
    }

    /**
     * 判断是否为行内转账
     */
    public boolean isInternal() {
        return bankCode == null && swiftCode == null && fpsId == null;
    }

    /**
     * 判断是否为跨境转账
     */
    public boolean isCrossBorder() {
        return swiftCode != null && !swiftCode.isBlank();
    }

    /**
     * 判断是否为 FPS 转账
     */
    public boolean isFps() {
        return fpsId != null && !fpsId.isBlank();
    }
}
