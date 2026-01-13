package com.macau.bank.user.domain.service;

import com.macau.bank.common.core.enums.KycLevel;
import com.macau.bank.common.core.enums.KycStatus;
import com.macau.bank.common.core.enums.UserLevel;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.user.domain.entity.UserInfo;
import com.macau.bank.user.domain.repository.UserInfoRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户领域服务
 * 负责用户档案的核心生命周期管理
 */
@Slf4j
@Service
public class UserDomainService {

    @Resource
    private UserInfoRepository userInfoRepository;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 根据用户编号获取档案
     */
    public UserInfo getByUserNo(String userNo) {
        return userInfoRepository.findByUserNo(userNo);
    }

    /**
     * 初始化用户基础档案 (注册后同步创建)
     */
    @Transactional(rollbackFor = Exception.class)
    public void createUserInfo(String userNo, String userName, String mobile) {
        Long count = userInfoRepository.countByUserNo(userNo);
        if (count > 0) {
            log.info("用户档案已存在，跳过创建: userNo={}", userNo);
            return;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserNo(userNo);
        userInfo.setKycLevel(KycLevel.ANONYMOUS);
        userInfo.setKycStatus(KycStatus.UNVERIFIED);
        userInfo.setUserLevel(UserLevel.NORMAL);
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());

        userInfoRepository.save(userInfo);
        log.info("用户基础档案创建成功: userNo={}, defaultLevel={}", userNo, UserLevel.NORMAL.getCode());
    }

    /**
     * 提交实名认证申请 (KYC)
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyCertification(UserInfo certificationInfo) {
        String userNo = certificationInfo.getUserNo();
        UserInfo existingUser = getByUserNo(userNo);
        if (existingUser == null) {
            throw new BusinessException("用户档案不存在");
        }

        KycStatus currentStatus = existingUser.getKycStatus();
        if (KycStatus.PENDING == currentStatus) {
            throw new BusinessException("认证审核中，请勿重复提交");
        }
        if (KycStatus.PASSED == currentStatus) {
            throw new BusinessException("已通过实名认证，无需重复提交");
        }

        // 使用实体内部方法更新，避免依赖 Application 层 Assembler
        existingUser.copyFromCertification(certificationInfo);

        existingUser.setKycStatus(KycStatus.PENDING);
        existingUser.setUpdateTime(LocalDateTime.now());

        userInfoRepository.update(existingUser);
        log.info("用户实名认证申请提交成功: userNo={}, kycStatus={}", userNo, KycStatus.PENDING.getDesc());
    }

    /**
     * 审核用户认证申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditUser(String userNo, boolean pass, String reason) {
        UserInfo userInfo = getByUserNo(userNo);
        if (userInfo == null) {
            throw new BusinessException("用户档案不存在");
        }

        if (KycStatus.PENDING != userInfo.getKycStatus()) {
            throw new BusinessException("该用户当前状态不可审核: " + userInfo.getKycStatus());
        }

        if (pass) {
            userInfo.setKycStatus(KycStatus.PASSED);
            userInfo.setKycLevel(KycLevel.PREMIUM);
            userInfo.setUserLevel(UserLevel.NORMAL);

            log.info("用户审核通过: userNo={}, level={}", userNo, KycLevel.PREMIUM.getDesc());

            sendSecurityMessage(userNo, userInfo.getUserLevel());
        } else {
            userInfo.setKycStatus(KycStatus.FAILED);
            log.info("用户审核驳回: userNo={}, reason={}", userNo, reason);
        }

        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoRepository.update(userInfo);
    }

    private void sendSecurityMessage(String userNo, UserLevel newLevel) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("userNo", userNo);
            msg.put("newLevel", newLevel);
            rocketMQTemplate.convertAndSend("USER_SECURITY_TOPIC", msg);
        } catch (Exception e) {
            log.warn("发送用户安全MQ消息失败: {}", e.getMessage());
        }
    }

    /**
     * 更新普通资料 (非实名部分)
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String userNo, String occupation, String region, String detail) {
        UserInfo userInfo = getByUserNo(userNo);
        if (userInfo == null) {
            throw new BusinessException("用户档案不存在");
        }

        if (occupation != null)
            userInfo.setOccupation(occupation);
        if (region != null)
            userInfo.setAddressRegion(region);
        if (detail != null)
            userInfo.setAddressDetail(detail);

        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoRepository.update(userInfo);
    }
}
