package com.macau.bank.message.interfaces.http.response;

import com.macau.bank.common.core.enums.MessageCategory;
import com.macau.bank.common.core.enums.YesNo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 消息响应
 * <p>
 * 用于展示站内信、通知或公告
 * </p>
 */
@Data
@Schema(description = "站内信/通知消息")
public class MessageResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "消息ID (业务唯一键)", example = "MSG20231027001")
    private String messageId;

    @Schema(description = "消息分类 (NOTICE, ALERT, PROMOTION)", example = "NOTICE")
    private MessageCategory type;

    @Schema(description = "标题", example = "转账成功通知")
    private String title;

    @Schema(description = "正文内容", example = "您的一笔转账已成功汇出...")
    private String content;

    @Schema(description = "是否已读 (1:是, 0:否)", example = "0")
    private YesNo isRead;

    @Schema(description = "发送时间", example = "2023-10-27 10:10:00")
    private String createTime;
}