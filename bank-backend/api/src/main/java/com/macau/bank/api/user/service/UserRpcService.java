package com.macau.bank.api.user.service;

import com.macau.bank.api.dto.UserQueryDTO;
import com.macau.bank.api.user.request.UserCreateRpcRequest;
import com.macau.bank.api.user.response.UserInfoRpcResponse;
import com.macau.bank.common.core.result.Result;

/**
 * 用户Dubbo服务接口
 * 供其他微服务远程调用
 */
public interface UserRpcService {

    /**
     * 创建用户档案
     */
    Result<Void> createUserInfo(UserCreateRpcRequest userInfoRpcRequest);
    
    /**
     * 根据查询条件查询用户信息
     * 
     * @param queryDTO 查询条件
     * @return 用户信息
     */
    UserInfoRpcResponse queryUserInfo(UserQueryDTO queryDTO);


    /**
     * 根据用户编号查询用户信息
     *
     * @param userNo 用户编号
     * @return 用户信息
     */
    UserInfoRpcResponse getUserByUserNo(String userNo);
}