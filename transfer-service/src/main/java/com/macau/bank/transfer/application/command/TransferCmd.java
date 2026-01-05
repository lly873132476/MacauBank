package com.macau.bank.transfer.application.command;

import com.macau.bank.common.core.enums.FeeType;
import com.macau.bank.common.core.enums.TransferType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 转账命令
 */
@Data
public class TransferCmd implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 转出账户号
     */
    private String fromAccountNo;

    /**
     * 转入账户号
     */
    private String toAccountNo;

    /**
     * 转入账户姓名
     */
    private String toAccountName;

    /**
     * 转入银行代码
     */
    private String toBankCode;

    /**
     * 转入银行SWIFT代码
     */
    private String swiftCode;

    /**
     * 转入银行FPS ID
     */
    private String fpsId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 货币代码
     */
    private String currencyCode;

    /**
     * 手续费承担方式: SHA/OUR/BEN
     */
    private FeeType feeType;

    /**
     * 交易密码
     */
    private String transactionPassword;

    /**
     * 备注
     */
    private String remark;

    /**
     * 幂等性键
     */
    private String idempotentKey;

    /**
     * 转账类型
     */
    private TransferType transferType;
    
    /**
     * 当前操作用户No (从Context获取)
     */
    private String userNo;
}
