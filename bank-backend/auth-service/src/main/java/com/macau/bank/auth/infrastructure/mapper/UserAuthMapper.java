package com.macau.bank.auth.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macau.bank.auth.domain.entity.UserAuth;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 */
public interface UserAuthMapper extends BaseMapper<UserAuth> {

    UserAuth selectByUserNo(@Param("userNo") String userNo);

}