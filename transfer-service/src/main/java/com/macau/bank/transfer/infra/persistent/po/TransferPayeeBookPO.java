package com.macau.bank.transfer.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.PayeeType;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.common.core.enums.YesNo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收款人名册持久化对象
 */
@Getter
@Setter
@ToString
@TableName("transfer_payee_book")
public class TransferPayeeBookPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userNo;
    private PayeeType payeeType;
    private YesNo isTop;
    private String aliasName;
    private String payeeName;
    private String avatar;
    private String accountNo;
    private String currencyCode;
    private TransferType transferType;
    private String bankName;
    private String bankCode;
    private String regionCode;
    private String swiftCode;
    private String clearingCode;
    private String bankAddress;
    private YesNo isInternal;
    private LocalDateTime lastTransTime;
    private Integer totalTransCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
