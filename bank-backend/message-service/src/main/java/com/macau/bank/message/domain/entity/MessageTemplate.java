package com.macau.bank.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;

import com.baomidou.mybatisplus.annotation.TableId;

import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.CommonStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@TableName("message_template")
public class MessageTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 模版代码 (如: SMS_OTP_LOGIN, MSG_TRANS_SUCCESS)
     */
    private String templateCode;

    /**
     * 渠道: SMS, EMAIL, STATION (站内信)
     */
    private String channel;

    /**
     * 标题模版 (站内信/邮件用)
     */
    private String titleTemplate;

    /**
     * 内容模版 (支持占位符, 如: 您的验证码是{code})
     */
    private String contentTemplate;

    /**
     * 语言: zh_HK, en_US, pt_MO
     */
    private String lang;

    /**
     * 状态: 1-启用 0-停用
     */
    private CommonStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
