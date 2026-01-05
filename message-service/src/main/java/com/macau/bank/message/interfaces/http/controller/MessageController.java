package com.macau.bank.message.interfaces.http.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.common.framework.web.annotation.CurrentUser;
import com.macau.bank.message.application.result.MessageResult;
import com.macau.bank.message.application.service.MessageAppService;
import com.macau.bank.message.interfaces.http.assembler.MessageWebAssembler;
import com.macau.bank.message.interfaces.http.response.MessageResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * 消息服务接口 - 重构版
 */
@Slf4j
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageAppService messageAppService;

    @Resource
    private MessageWebAssembler messageWebAssembler;

    /**
     * 分页查询消息列表
     * userNo 通常由网关通过请求头 X-User-No 传递
     */
    @GetMapping("/page")
    public Result<IPage<MessageResponse>> getMessagePage(
            @CurrentUser String userNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        IPage<MessageResult> resultPage = messageAppService.getMessagePage(userNo, page, pageSize);
        
        // 转换 IPage 中的数据
        IPage<MessageResponse> responsePage = resultPage.convert(messageWebAssembler::toResponse);
        
        return Result.success(responsePage);
    }

    /**
     * 标记已读
     */
    @PostMapping("/read")
    public Result<Void> markAsRead(@CurrentUser String userNo, @RequestParam String messageId) {
        
        Assert.hasText(messageId, "消息ID不能为空");
        
        messageAppService.markAsRead(userNo, messageId);
        return Result.success(null);
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(@CurrentUser String userNo) {
        
        Integer count = messageAppService.getUnreadCount(userNo);
        return Result.success(count);
    }
}
