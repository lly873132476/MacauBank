package com.macau.bank.account.application.query;

import com.macau.bank.common.framework.web.model.BaseRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 资产总览查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class AssetSummaryQuery extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户编号 */
    private String userNo;
}
