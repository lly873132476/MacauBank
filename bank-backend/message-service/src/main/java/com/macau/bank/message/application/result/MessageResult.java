package com.macau.bank.message.application.result;

import com.macau.bank.common.core.enums.MessageCategory;
import com.macau.bank.common.core.enums.YesNo;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String msgId;
    private String userNo;
    private MessageCategory category;
    private String title;
    private String content;
    private String bizType;
    private String bizId;
    private YesNo isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
