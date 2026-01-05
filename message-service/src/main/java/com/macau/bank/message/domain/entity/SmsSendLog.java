package com.macau.bank.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.macau.bank.common.core.enums.SmsStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
@TableName(value = "sms_send_log", autoResultMap = true)
public class SmsSendLog implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 全局消息ID (Trace ID)
     */
    private String msgId;

    /**
     * 接收用户编号 (如果是游客发送验证码，此字段可空)
     */
    private String userNo;

    /**
     * 区号
     */
    private String mobilePrefix;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 关联的模版Code
     */
    private String templateCode;
    
    /**
     * 模版参数快照 (如: {"code": "1234"})
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> contentParams;
    
    /**
     * 最终发送的完整短信内容 (用于审计)
     */
    private String finalContent;

    /**
     * 供应商: ALIYUN, TWILIO, CTM
     */
    private String provider;

    /**
     * 供应商返回的流水号
     */
    private String providerMsgId;

    /**
     * 供应商接口返回的完整报文 (Debug神器)
     */
    private String providerResponse;

    /**
     * 状态: 0-发送中 1-成功 2-失败
     */
    private SmsStatus status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
}
