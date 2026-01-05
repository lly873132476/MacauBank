package com.macau.bank.account.application.query;

import com.macau.bank.common.framework.web.model.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 账户详情查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AccountSummaryQuery extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 账户号 */
    private String accountNo;
}
