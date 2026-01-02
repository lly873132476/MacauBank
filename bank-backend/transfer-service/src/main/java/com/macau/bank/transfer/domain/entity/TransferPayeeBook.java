package com.macau.bank.transfer.domain.entity;

import com.macau.bank.common.core.enums.PayeeType;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.common.core.enums.YesNo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收款人名册实体 (常用收款人)
 */
@Getter
@Setter
@ToString
public class TransferPayeeBook implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 所属用户编号
     */
    private String userNo;

    /**
     * 收款人类型: 0-历史记录(自动保存) 1-常用联系人(手动收藏)
     */
    private PayeeType payeeType;

    /**
     * 是否置顶: 1-是 0-否
     */
    private YesNo isTop;

    /**
     * 别名 (如: 房东, 儿子)
     */
    private String aliasName;

    /**
     * 收款人户名 (严格匹配)
     */
    private String payeeName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 收款账号/IBAN
     */
    private String accountNo;

    /**
     * 默认币种
     */
    private String currencyCode;

    /**
     * 默认转账类型
     */
    private TransferType transferType;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 银行代码
     */
    private String bankCode;

    /**
     * 地区代码
     */
    private String regionCode;

    /**
     * SWIFT代码
     */
    private String swiftCode;

    /**
     * 清算代码
     */
    private String clearingCode;

    /**
     * 银行地址
     */
    private String bankAddress;

    /**
     * 是否行内账户: 1-是 0-否
     */
    private YesNo isInternal;

    /**
     * 最后转账时间
     */
    private LocalDateTime lastTransTime;

    /**
     * 总转账次数
     */
    private Integer totalTransCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}