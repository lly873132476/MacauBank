package com.macau.bank.auth.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.auth.domain.entity.UserAuth;
import com.macau.bank.auth.domain.repository.UserAuthRepository;
import com.macau.bank.auth.infrastructure.mapper.UserAuthMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * 用户认证仓储实现
 * <p>
 * 归属：Infrastructure 层
 * 职责：实现 UserAuthRepository 接口，封装 MyBatis-Plus 操作
 */
@Repository
public class UserAuthRepositoryImpl implements UserAuthRepository {

    @Resource
    private UserAuthMapper userAuthMapper;

    @Override
    public UserAuth findByUserName(String userName) {
        return userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getUserName, userName));
    }

    @Override
    public UserAuth findByMobile(String mobile) {
        return userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getMobile, mobile));
    }

    @Override
    public UserAuth findByUserNameOrMobile(String userName, String mobile) {
        return userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getUserName, userName)
                .or()
                .eq(UserAuth::getMobile, mobile));
    }

    @Override
    public UserAuth findByUserNo(String userNo) {
        return userAuthMapper.selectByUserNo(userNo);
    }

    @Override
    public void save(UserAuth userAuth) {
        if (userAuth.getId() == null) {
            userAuthMapper.insert(userAuth);
        } else {
            userAuthMapper.updateById(userAuth);
        }
    }
}
