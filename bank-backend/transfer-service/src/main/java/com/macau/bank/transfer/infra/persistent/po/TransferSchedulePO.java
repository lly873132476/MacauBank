package com.macau.bank.transfer.infra.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * 预约与周期转账计划持久化对象
 */
@Getter
@Setter
@ToString
@TableName("transfer_schedule")
public class TransferSchedulePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userNo;
    private ScheduleTypeEnum scheduleType;
    private String cronExpression;
    private LocalDateTime executeTime;
    private String payerAccountNo;
    private String payeeAccountNo;
    private String payeeName;
    private BigDecimal amount;
    private String currencyCode;
    private TransferChannel transferChannel;
    private ScheduleStatusEnum status;
    private Integer retryCount;
    private String remark;
    private TransferStatus lastExecuteOrderId; // Note: Original Entity type was TransferStatus, checking context it might be a String or ID, but keeping as Entity for now if code logic matches. Wait, name is 'lastExecuteOrderId', type is 'TransferStatus'? This looks like a bug in original entity.
    private Integer lastExecuteStatus;
    private LocalDateTime lastExecuteTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
