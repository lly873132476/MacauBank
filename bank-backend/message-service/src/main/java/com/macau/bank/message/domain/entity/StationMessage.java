package com.macau.bank.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.Deleted;
import com.macau.bank.common.core.enums.MessageCategory;
import com.macau.bank.common.core.enums.YesNo;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("station_message")
public class StationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联业务ID (如 transfer_order.txn_id)
     */
    private String bizId;

    /**
     * 全局消息ID
     */
    private String msgId;

    /**
     * 接收用户
     */
    private String userNo;

    /**
     * 分类: 1-动账通知 2-安全中心 3-系统公告
     */
    private MessageCategory category;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息正文 (或者HTML片段)
     */
    private String content;

    /**
     * 业务类型: TRANS_DETAIL, KYC_RESULT
     */
    private BizType bizType;

    /**
     * 是否已读: 0-未读 1-已读
     */
    private YesNo isRead;

    /**
     * 读取时间
     */
    private LocalDateTime readTime;

    /**
     * 用户逻辑删除
     */
    @TableLogic
    private Deleted isDeleted;

    /**
     * 推送时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
