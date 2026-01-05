package com.macau.bank.message.domain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.common.core.enums.YesNo;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.message.domain.entity.MessageTemplate;
import com.macau.bank.message.domain.entity.StationMessage;
import com.macau.bank.message.infrastructure.mapper.MessageTemplateMapper;
import com.macau.bank.message.infrastructure.mapper.StationMessageMapper;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息领域服务 - 深度优化版 (基于模版与多渠道设计)
 */
@Slf4j
@Service
public class MessageDomainService {

    @Resource
    private StationMessageMapper stationMessageMapper;

    @Resource
    private MessageTemplateMapper messageTemplateMapper;

    /**
     * 根据模版创建消息
     * @param userNo 用户编号
     * @param templateCode 模版代码
     * @param params 替换参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void createMessageByTemplate(String userNo, String templateCode, Map<String, Object> params, String bizId) {
        // 1. 获取模版
        MessageTemplate template = messageTemplateMapper.selectOne(new LambdaQueryWrapper<MessageTemplate>()
                .eq(MessageTemplate::getTemplateCode, templateCode)
                .eq(MessageTemplate::getStatus, 1));
        
        if (template == null) {
            log.error("消息模版不存在或未启用: {}", templateCode);
            return;
        }

        // 2. 渲染内容 (简单占位符替换)
        String content = renderTemplate(template.getContentTemplate(), params);
        String title = renderTemplate(template.getTitleTemplate(), params);

        // 3. 根据渠道分发
        if (template.getChannel().contains("STATION")) {
            StationMessage msg = StationMessage.builder()
                    .msgId(IdUtil.getSnowflakeNextIdStr())
                    .userNo(userNo)
                    .bizId(bizId)
                    .title(title)
                    .content(content)
                    .isRead(YesNo.NO)
                    .createTime(LocalDateTime.now())
                    .build();
            stationMessageMapper.insert(msg);
        }

        // TODO: 如果 channel 包含 SMS，则调用短信供应商接口并记录 sms_send_log
    }

    private String renderTemplate(String template, Map<String, Object> params) {
        if (template == null) return "";
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }

    /**
     * 创建消息 (原始方法，保留用于直接发送自定义内容的场景)
     */
    @Transactional(rollbackFor = Exception.class)
    public StationMessage createMessage(StationMessage message) {
        log.info("领域服务 - 创建消息: userNo={}, title={}", message.getUserNo(), message.getTitle());
        if (message.getMsgId() == null) {
            message.setMsgId(IdUtil.getSnowflakeNextIdStr());
        }
        stationMessageMapper.insert(message);
        return message;
    }

    /**
     * 检查业务消息是否存在 (幂等性校验)
     */
    public boolean isMessageExists(String bizId, String userNo) {
        Long count = stationMessageMapper.selectCount(new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getBizId, bizId)
                .eq(StationMessage::getUserNo, userNo));
        return count != null && count > 0;
    }


    /**
     * 分页查询消息 (优化性能)
     */
    public IPage<StationMessage> getMessagePage(String userNo, int page, int pageSize) {
        Page<StationMessage> pageParam = new Page<>(page, pageSize);
        return stationMessageMapper.selectPage(pageParam, new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getUserNo, userNo)
                .orderByDesc(StationMessage::getCreateTime));
    }

    /**
     * 标记消息已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(String userNo, String messageId) {
        log.info("领域服务 - 标记已读: userNo={}, messageId={}", userNo, messageId);

        // 1. 查找消息 (由于 Mapper 里可能有逻辑删除，此处使用 MP 自带的 update)
        // 注意：此处应根据 bizId 或 id 更新，假设外部传的是 bizId
        StationMessage updateMsg = new StationMessage();
        updateMsg.setIsRead(YesNo.YES);
        updateMsg.setReadTime(LocalDateTime.now());
        updateMsg.setUpdateTime(LocalDateTime.now());

        int rows = stationMessageMapper.update(updateMsg, new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getBizId, messageId)
                .eq(StationMessage::getUserNo, userNo));
        
        if (rows == 0) {
            throw new BusinessException("未找到可更新的消息");
        }
    }

    /**
     * 获取未读消息数量
     */
    public int getUnreadCount(String userNo) {
        Long count = stationMessageMapper.selectCount(new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getUserNo, userNo)
                .eq(StationMessage::getIsRead, YesNo.NO));
        return count != null ? count.intValue() : 0;
    }
    
    /**
     * 查询全量消息列表 (建议废弃，优先使用 Page)
     */
    @Deprecated
    public List<StationMessage> getMessageList(String userNo) {
        return stationMessageMapper.selectList(new LambdaQueryWrapper<StationMessage>()
                .eq(StationMessage::getUserNo, userNo)
                .orderByDesc(StationMessage::getCreateTime));
    }
}