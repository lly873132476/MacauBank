package com.macau.bank.user.interfaces.http.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRequest {

    @NotBlank(message = "用户编号不能为空")
    private String userNo;

    /**
     * true: 审核通过
     * false: 审核拒绝
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean pass;

    /**
     * 审核备注/驳回原因
     */
    private String remark;
}