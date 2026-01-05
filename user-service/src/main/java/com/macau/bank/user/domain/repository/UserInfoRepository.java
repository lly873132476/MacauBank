package com.macau.bank.user.domain.repository;

import com.macau.bank.user.domain.entity.UserInfo;

/**
 * 用户信息仓储接口
 * <p>
 * 归属：Domain 层
 * 职责：定义用户档案数据访问契约，隔离 Mapper 实现
 */
public interface UserInfoRepository {

    /**
     * 根据用户编号查询用户档案
     */
    UserInfo findByUserNo(String userNo);

    /**
     * 统计用户编号的记录数
     */
    Long countByUserNo(String userNo);

    /**
     * 保存用户档案（新增）
     */
    void save(UserInfo userInfo);

    /**
     * 更新用户档案
     */
    void update(UserInfo userInfo);
}
