package com.macau.bank.transfer.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.FeeType;
import com.macau.bank.transfer.common.enums.ConfigStatusEnum;
import com.macau.bank.transfer.common.enums.FeeCalcModeEnum;
import com.macau.bank.transfer.common.enums.FeeDeductTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 转账手续费算费规则持久化对象
 */
@Getter
@Setter
@ToString
@TableName("transfer_fee_config")
public class TransferFeeConfigPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String transferChannel;
    private String currencyCode;
    private String userLevel;
    private FeeType feeType;
    private String feeCurrency;
    private FeeDeductTypeEnum deductType;
    private FeeCalcModeEnum calcMode;
    private BigDecimal fixedAmount;
    private BigDecimal rate;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    private String description;
    private ConfigStatusEnum status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
