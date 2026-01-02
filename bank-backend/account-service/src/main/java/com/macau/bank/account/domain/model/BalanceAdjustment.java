package com.macau.bank.account.domain.model;

import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.BizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 资金调整值对象 (Value Object)
 * <p>
 * 封装单次资金调整所需的完整信息，传递给 Domain Service 执行业务逻辑
 * </p>
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BalanceAdjustment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标账号
     */
    private String accountNo;

    /**
     * 变动金额 (包含币种)
     * 正数=入账，负数=出账
     */
    private Money amount;

    /**
     * 业务描述
     */
    private String description;

    /**
     * 业务流水号 (上游业务唯一ID)
     */
    private String bizNo;

    /**
     * 请求幂等ID (防重)
     */
    private String requestId;

    /**
     * 业务类型（由上游传递）
     */
    private BizType bizType;

    /**
     * 校验自身有效性
     */
    public void validate() {
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("请求ID不能为空");
        }
    }
}
