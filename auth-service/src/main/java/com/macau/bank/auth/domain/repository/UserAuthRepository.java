package com.macau.bank.auth.domain.repository;

import com.macau.bank.auth.domain.entity.UserAuth;

/**
 * 用户认证仓储接口
 * <p>
 * 归属：Domain 层
 * 职责：定义用户认证数据访问契约，隔离 Mapper 实现
 */
public interface UserAuthRepository {

    /**
     * 根据用户名查询认证信息
     */
    UserAuth findByUserName(String userName);

    /**
     * 根据手机号查询认证信息
     */
    UserAuth findByMobile(String mobile);

    /**
     * 根据用户名或手机号查询认证信息
     */
    UserAuth findByUserNameOrMobile(String userName, String mobile);

    /**
     * 根据用户编号查询认证信息
     */
    UserAuth findByUserNo(String userNo);

    /**
     * 统计用户名数量
     */
    Long countByUserName(String userName);

    /**
     * 统计手机号数量
     */
    Long countByMobile(String mobile);

    /**
     * 保存用户认证信息（新增）
     */
    void save(UserAuth userAuth);

    /**
     * 更新用户认证信息
     */
    void update(UserAuth userAuth);

    /**
     * 根据用户编号更新用户认证信息
     */
    void updateByUserNo(String userNo, UserAuth userAuth);
}
