package com.macau.bank.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用户审核通过事件
 * 发生时间：运营人员点击审核通过，且落库成功后
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAuditedEvent {
    
    /** 用户唯一编号 */
    private String userNo;

    /** 审批通过时间 */
    private LocalDateTime auditedTime;
    // 如果之后转账需要手机号或名字，也可以加在这里
}