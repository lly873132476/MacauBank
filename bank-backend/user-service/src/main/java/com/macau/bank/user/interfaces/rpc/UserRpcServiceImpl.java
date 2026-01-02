package com.macau.bank.user.interfaces.rpc;

import com.macau.bank.api.dto.UserQueryDTO;
import com.macau.bank.api.user.request.UserCreateRpcRequest;
import com.macau.bank.api.user.response.UserInfoRpcResponse;
import com.macau.bank.api.user.service.UserRpcService;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.user.interfaces.rpc.assembler.UserRpcAssembler;
import com.macau.bank.user.application.command.CreateUserInfoCmd;
import com.macau.bank.user.application.service.UserInfoAppService;
import com.macau.bank.user.domain.entity.UserInfo;
import com.macau.bank.user.domain.service.UserDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户中心 RPC 服务实现
 */
@Slf4j
@DubboService
public class UserRpcServiceImpl implements UserRpcService {

    @Resource
    private UserInfoAppService userInfoAppService;

    @Resource
    private UserDomainService userDomainService;

    @Resource
    private UserRpcAssembler userRpcAssembler;

    @Override
    public Result<Void> createUserInfo(UserCreateRpcRequest request) {
        log.info("RPC调用 - 创建基础用户资料: userNo={}", request.getUserNo());
        
        CreateUserInfoCmd cmd = userRpcAssembler.toCmd(request);
                
        userInfoAppService.createUserInfo(cmd);
        
        return Result.success();
    }

    @Override
    public UserInfoRpcResponse queryUserInfo(UserQueryDTO queryDTO) {
        log.info("RPC调用 - 查询用户信息: queryDTO={}", queryDTO);
        // 此处逻辑暂简写为根据 userNo 查询
        UserInfo userInfo = userDomainService.getByUserNo(queryDTO.getUserNo());
        return userRpcAssembler.toRpc(userInfo);
    }

    @Override
    public UserInfoRpcResponse getUserByUserNo(String userNo) {
        log.info("RPC调用 - 根据编号查询用户: userNo={}", userNo);
        UserInfo userInfo = userDomainService.getByUserNo(userNo);
        return userRpcAssembler.toRpc(userInfo);
    }
}
