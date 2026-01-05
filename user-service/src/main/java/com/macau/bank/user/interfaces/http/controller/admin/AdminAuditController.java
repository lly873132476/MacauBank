package com.macau.bank.user.interfaces.http.controller.admin;

import com.macau.bank.common.core.result.Result;
import com.macau.bank.user.application.command.AuditUserCmd;
import com.macau.bank.user.application.service.UserInfoAppService;
import com.macau.bank.user.interfaces.http.request.AuditRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [内部接口] 用于模拟后台审核流程
 * 警告：生产环境应屏蔽此接口或加严格鉴权
 */
@Slf4j
@RestController
@RequestMapping("/admin/audit")
public class AdminAuditController {

    @Resource
    private UserInfoAppService userInfoAppService;

    /**
     * 人工审核通用接口 (通过/拒绝)
     */
    @PostMapping("/process")
    public Result<Void> processUserAudit(@RequestBody @Validated AuditRequest request) {

        AuditUserCmd cmd = AuditUserCmd.builder()
                .userNo(request.getUserNo())
                .pass(request.getPass())
                .remark(request.getRemark())
                .build();
        userInfoAppService.auditUser(cmd);
        
        return Result.success();
    }
}