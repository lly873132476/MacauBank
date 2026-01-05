package com.macau.bank.transfer.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.transfer.common.enums.ConfigStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 转账限额规则配置持久化对象
 */
@Getter
@Setter
@ToString
@TableName("transfer_limit_config")
public class TransferLimitConfigPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String userLevel;
    private TransferType transferType;
    private String currency;
    private BigDecimal singleLimit;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private ConfigStatusEnum status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
