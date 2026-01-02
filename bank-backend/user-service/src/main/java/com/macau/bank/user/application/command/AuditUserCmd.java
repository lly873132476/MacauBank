package com.macau.bank.user.application.command;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

/**
 * 用户审核指令
 */
@Data
@Builder
public class AuditUserCmd implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 待审核用户编号
     */
    private String userNo;

    /**
     * 审核结果: true-通过, false-驳回
     */
    private Boolean pass;

    /**
     * 审核备注/驳回原因
     */
    private String remark;
}
