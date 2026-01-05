package com.macau.bank.transfer.infra.rpc;

import com.macau.bank.api.user.response.UserInfoRpcResponse;
import com.macau.bank.api.user.service.UserRpcService;
import com.macau.bank.transfer.domain.gateway.UserGateway;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 用户网关实现
 * 归属：Infrastructure 层
 * 作用：真正的 Dubbo 调用
 */
@Component
public class UserGatewayImpl implements UserGateway {

    @DubboReference
    private UserRpcService userRpcService;

    @Override
    public UserInfoRpcResponse getUserByUserNo(String userNo) {
        // 这里可以做防腐：把 RPC 异常转成 RuntimeException，或者处理 null
        return userRpcService.getUserByUserNo(userNo);
    }

}