package com.macau.bank.message.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.macau.bank.common.core.enums.YesNo;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.message.domain.entity.MessageTemplate;
import com.macau.bank.message.domain.entity.StationMessage;
import com.macau.bank.message.domain.repository.MessageTemplateRepository;
import com.macau.bank.message.domain.repository.StationMessageRepository;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息领域服务
 * <p>
 * 职责：消息核心业务逻辑
 * - 基于模板创建消息
 * - 消息查询
 * - 消息已读状态管理
 */
@Slf4j
@Service
public class MessageDomainService {

    @Resource
    private StationMessageRepository stationMessageRepository;

    @Resource
    private MessageTemplateRepository messageTemplateRepository;

    /**
     * 根据模版创建消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void createMessageByTemplate(String userNo, String templateCode, Map<String, Object> params, String bizId) {
        // 1. 获取模版
        MessageTemplate template = messageTemplateRepository.findByCodeAndStatus(templateCode, 1);

        if (template == null) {
            log.error("消息模版不存在或未启用: {}", templateCode);
            return;
        }

        // 2. 渲染内容
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
            stationMessageRepository.save(msg);
        }
    }

    private String renderTemplate(String template, Map<String, Object> params) {
        if (template == null)
            return "";
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }

    /**
     * 创建消息（直接发送自定义内容）
     */
    @Transactional(rollbackFor = Exception.class)
    public StationMessage createMessage(StationMessage message) {
        log.info("领域服务 - 创建消息: userNo={}, title={}", message.getUserNo(), message.getTitle());
        if (message.getMsgId() == null) {
            message.setMsgId(IdUtil.getSnowflakeNextIdStr());
        }
        stationMessageRepository.save(message);
        return message;
    }

    /**
     * 检查业务消息是否存在（幂等性校验）
     */
    public boolean isMessageExists(String bizId, String userNo) {
        Long count = stationMessageRepository.countByBizIdAndUserNo(bizId, userNo);
        return count != null && count > 0;
    }

    /**
     * 分页查询消息
     */
    public IPage<StationMessage> getMessagePage(String userNo, int page, int pageSize) {
        return stationMessageRepository.pageByUserNo(userNo, page, pageSize);
    }

    /**
     * 标记消息已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(String userNo, String messageId) {
        log.info("领域服务 - 标记已读: userNo={}, messageId={}", userNo, messageId);

        int rows = stationMessageRepository.updateReadStatus(messageId, userNo);

        if (rows == 0) {
            throw new BusinessException("未找到可更新的消息");
        }
    }

    /**
     * 获取未读消息数量
     */
    public int getUnreadCount(String userNo) {
        Long count = stationMessageRepository.countUnread(userNo);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 查询全量消息列表（建议使用分页查询）
     */
    @Deprecated
    public List<StationMessage> getMessageList(String userNo) {
        return stationMessageRepository.findByUserNo(userNo);
    }
}
