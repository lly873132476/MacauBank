package com.macau.bank.user.application.service;

import com.macau.bank.user.application.command.AuditUserCmd;
import com.macau.bank.user.application.command.CreateUserInfoCmd;
import com.macau.bank.user.application.command.UpdateProfileCmd;
import com.macau.bank.user.application.command.UserCertificationCmd;
import com.macau.bank.user.application.result.UserProfileResult;

/**
 * 用户信息应用服务接口
 */
public interface UserInfoAppService {

    /**
     * 创建用户基础档案 (RPC调用)
     */
    void createUserInfo(CreateUserInfoCmd cmd);

    /**
     * 用户实名认证申请 (HTTP调用)
     */
    void certifyUser(UserCertificationCmd cmd);

    /**
     * 审核用户认证申请 (后台调用)
     */
    void auditUser(AuditUserCmd cmd);

    /**
     * 获取用户个人资料详情
     */
    UserProfileResult getUserProfile(String userNo);
    
    /**
     * 更新用户资料 (HTTP调用)
     */
    void updateUserProfile(UpdateProfileCmd cmd);
}
