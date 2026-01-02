package com.macau.bank.user.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.common.core.enums.KycLevel;
import com.macau.bank.common.core.enums.KycStatus;
import com.macau.bank.common.core.enums.UserLevel;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.user.application.assembler.UserDomainAssembler;
import com.macau.bank.user.domain.entity.UserInfo;
import com.macau.bank.user.infrastructure.mapper.UserInfoMapper;
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
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserDomainAssembler userDomainAssembler;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 根据用户编号获取档案
     */
    public UserInfo getByUserNo(String userNo) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserNo, userNo));
    }

    /**
     * 初始化用户基础档案 (注册后同步创建)
     */
    @Transactional(rollbackFor = Exception.class)
    public void createUserInfo(String userNo, String userName, String mobile) {
        Long count = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserNo, userNo));
        if (count > 0) {
            log.info("用户档案已存在，跳过创建: userNo={}", userNo);
            return;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserNo(userNo);
        userInfo.setKycLevel(KycLevel.ANONYMOUS);
        userInfo.setKycStatus(KycStatus.UNVERIFIED);
        userInfo.setUserLevel(UserLevel.NORMAL); // 填充默认用户等级
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        
        userInfoMapper.insert(userInfo);
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

        userDomainAssembler.updateEntity(certificationInfo, existingUser);
        
        existingUser.setKycStatus(KycStatus.PENDING);
        existingUser.setUpdateTime(LocalDateTime.now());

        userInfoMapper.updateById(existingUser);
        log.info("用户实名认证申请提交成功: userNo={}, kycStatus={}", userNo, KycStatus.PENDING.getDesc());
    }

    /**
     * 审核用户认证申请
     * 执行 KYC 状态流转:
     * PENDING -> PASSED: 提升 KycLevel, 触发开户
     * PENDING -> FAILED: 保持 KycLevel, 记录失败原因
     *
     * @param userNo 用户编号
     * @param pass   是否通过
     * @param reason 原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditUser(String userNo, boolean pass, String reason) {
        UserInfo userInfo = getByUserNo(userNo);
        if (userInfo == null) {
            throw new BusinessException("用户档案不存在");
        }

        // 只有PENDING状态才能被审核
        if (KycStatus.PENDING != userInfo.getKycStatus()) {
            throw new BusinessException("该用户当前状态不可审核: " + userInfo.getKycStatus());
        }

        if (pass) {
            // 审核通过
            userInfo.setKycStatus(KycStatus.PASSED);
            // 提升等级：简单逻辑直接升到 PREMIUM，复杂逻辑可根据材料判断
            userInfo.setKycLevel(KycLevel.PREMIUM);
            
            // [新增] 提升用户业务等级 (如实名后升为 NORMAL)
            userInfo.setUserLevel(UserLevel.NORMAL);
            
            log.info("用户审核通过: userNo={}, level={}", userNo, KycLevel.PREMIUM.getDesc());
            
            // 发送安全事件通知
            sendSecurityMessage(userNo, userInfo.getUserLevel());
        } else {
            // 审核驳回
            userInfo.setKycStatus(KycStatus.FAILED);
            log.info("用户审核驳回: userNo={}, reason={}", userNo, reason);
        }
        
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoMapper.updateById(userInfo);
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
        
        if (occupation != null) userInfo.setOccupation(occupation);
        if (region != null) userInfo.setAddressRegion(region);
        if (detail != null) userInfo.setAddressDetail(detail);
        
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoMapper.updateById(userInfo);
    }
}
