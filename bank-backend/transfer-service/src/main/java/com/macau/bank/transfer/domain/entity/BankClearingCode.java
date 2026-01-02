package com.macau.bank.transfer.domain.entity;

import com.macau.bank.transfer.common.enums.ConfigStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 银行清算行号数据表
 */
@Getter
@Setter
@ToString
public class BankClearingCode implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 地区: MO-澳门, HK-香港, CN-内地
     */
    private String regionCode;

    /**
     * 银行简码
     */
    private String bankCode;

    /**
     * 银行中文名
     */
    private String bankNameCn;

    /**
     * 银行英文名/葡文名
     */
    private String bankNameEn;

    /**
     * 银行Logo图标
     */
    private String logoUrl;

    /**
     * 本地清算号 (FPS ID / RTGS Code)
     */
    private String clearingCode;

    /**
     * SWIFT BIC Code
     */
    private String swiftCode;

    /**
     * 是否热门银行
     */
    private Integer isHot;

    /**
     * 支持渠道: FPS,SWIFT (逗号分隔)
     */
    private String supportChannel;

    /**
     * 1-支持转账 0-维护中
     */
    private ConfigStatusEnum status;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}