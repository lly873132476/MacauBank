package com.macau.bank.user.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.user.domain.entity.UserInfo;
import com.macau.bank.user.domain.repository.UserInfoRepository;
import com.macau.bank.user.infrastructure.mapper.UserInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * 用户信息仓储实现
 * <p>
 * 归属：Infrastructure 层
 * 职责：实现 UserInfoRepository 接口，封装 MyBatis-Plus 操作
 */
@Repository
public class UserInfoRepositoryImpl implements UserInfoRepository {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public UserInfo findByUserNo(String userNo) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserNo, userNo));
    }

    @Override
    public Long countByUserNo(String userNo) {
        return userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserNo, userNo));
    }

    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    @Override
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }
}
