package com.macau.bank.account.domain.entity;

import com.macau.bank.common.core.enums.CommonStatus;
import com.macau.bank.common.core.enums.YesNo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账户支持币种配置领域对象
 */
@Getter
@Setter
@ToString
public class AccountCurrencyConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String currencyCode;
    private String currencyName;
    private String currencySymbol;
    private YesNo isLocal;
    private Integer sortOrder;
    private CommonStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}