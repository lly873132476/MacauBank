package com.macau.bank.user.application.service.impl;

import com.macau.bank.common.core.constant.MqTopicConst;
import com.macau.bank.common.framework.lock.annotation.RedissonLock;
import com.macau.bank.common.event.UserAuditedEvent;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.user.application.assembler.UserDomainAssembler;
import com.macau.bank.user.application.command.AuditUserCmd;
import com.macau.bank.user.application.command.CreateUserInfoCmd;
import com.macau.bank.user.application.command.UpdateProfileCmd;
import com.macau.bank.user.application.command.UserCertificationCmd;
import com.macau.bank.user.application.result.UserProfileResult;
import com.macau.bank.user.application.service.UserInfoAppService;
import com.macau.bank.user.domain.entity.UserInfo;
import com.macau.bank.user.domain.service.UserDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserInfoAppServiceImpl implements UserInfoAppService {

    @Resource
    private UserDomainService userDomainService;
    
    @Resource
    private UserDomainAssembler userDomainAssembler;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUserInfo(CreateUserInfoCmd cmd) {
        log.info("应用服务 - 创建基础用户档案: userNo={}", cmd.getUserNo());
        userDomainService.createUserInfo(cmd.getUserNo(), cmd.getUserName(), cmd.getMobile());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void certifyUser(UserCertificationCmd cmd) {
        log.info("应用服务 - 用户实名认证申请: userNo={}", cmd.getUserNo());
        UserInfo userInfo = userDomainAssembler.toEntity(cmd);
        userDomainService.applyCertification(userInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "'audit_user_' + #cmd.userNo", waitTime = 3, msg = "正在审核中，请勿重复操作")
    public void auditUser(AuditUserCmd cmd) {
        log.info("应用服务 - 审核用户: userNo={}, pass={}", cmd.getUserNo(), cmd.getPass());

        // 1. 执行领域逻辑：更新审核状态
        userDomainService.auditUser(cmd.getUserNo(), cmd.getPass(), cmd.getRemark());

        // 2. 如果审核通过，发送 MQ 消息
        if (cmd.getPass()) {
            UserAuditedEvent event = new UserAuditedEvent(cmd.getUserNo(), LocalDateTime.now());

            // 发送消息
            rocketMQTemplate.convertAndSend(MqTopicConst.TP_USER_AUDIT_PASS, event);
            log.info("MQ消息已发送 - 用户审核通过: {}", event.getUserNo());
        }
    }

    @Override
    public UserProfileResult getUserProfile(String userNo) {
        log.info("应用服务 - 获取个人资料详情: userNo={}", userNo);
        UserInfo userInfo = userDomainService.getByUserNo(userNo);
        if (userInfo == null) {
            throw new BusinessException("用户档案不存在");
        }
        return userDomainAssembler.toResult(userInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserProfile(UpdateProfileCmd cmd) {
        log.info("应用服务 - 更新用户资料: userNo={}", cmd.getUserNo());
        userDomainService.updateProfile(
            cmd.getUserNo(), 
            cmd.getOccupation(), 
            cmd.getAddressRegion(), 
            cmd.getAddressDetail()
        );
    }
}
