package com.macau.bank.transfer.domain.entity;

import com.macau.bank.common.core.enums.TransferChannel;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.transfer.common.enums.ScheduleStatusEnum;
import com.macau.bank.transfer.common.enums.ScheduleTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约与周期转账计划表
 */
@Getter
@Setter
@ToString
public class TransferSchedule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户编号
     */
    private String userNo;

    /**
     * 类型: 1-单次预约(Appointment) 2-周期性(Standing Order)
     */
    private ScheduleTypeEnum scheduleType;

    /**
     * 周期规则Cron表达式
     */
    private String cronExpression;

    /**
     * 下一次执行时间(预约时间)
     */
    private LocalDateTime executeTime;

    /**
     * 付款账号
     */
    private String payerAccountNo;

    /**
     * 收款账号
     */
    private String payeeAccountNo;

    /**
     * 收款户名快照
     */
    private String payeeName;

    /**
     * 转账金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currencyCode;

    /**
     * 渠道
     */
    private TransferChannel transferChannel;

    /**
     * 状态: 1-生效中 0-暂停 2-已结束
     */
    private ScheduleStatusEnum status;

    /**
     * 连续失败次数
     */
    private Integer retryCount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 最近一次成功生成的订单号
     */
    private TransferStatus lastExecuteOrderId;

    /**
     * 上次执行状态
     */
    private Integer lastExecuteStatus;

    /**
     * 上次执行时间
     */
    private LocalDateTime lastExecuteTime;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}