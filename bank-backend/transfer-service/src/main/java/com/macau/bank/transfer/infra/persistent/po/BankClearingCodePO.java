package com.macau.bank.transfer.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.transfer.common.enums.ConfigStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 银行清算行号数据持久化对象
 */
@Getter
@Setter
@ToString
@TableName("bank_clearing_code")
public class BankClearingCodePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String regionCode;
    private String bankCode;
    private String bankNameCn;
    private String bankNameEn;
    private String logoUrl;
    private String clearingCode;
    private String swiftCode;
    private Integer isHot;
    private String supportChannel;
    private ConfigStatusEnum status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
