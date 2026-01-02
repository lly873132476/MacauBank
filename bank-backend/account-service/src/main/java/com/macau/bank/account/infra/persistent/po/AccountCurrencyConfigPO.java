package com.macau.bank.account.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.CommonStatus;
import com.macau.bank.common.core.enums.YesNo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账户支持币种配置持久化对象
 */
@Getter
@Setter
@ToString
@TableName("account_currency_config")
public class AccountCurrencyConfigPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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
