package com.macau.bank.transfer.domain.gateway;

import com.macau.bank.api.user.response.UserInfoRpcResponse;

/**
 * 用户网关接口
 * 归属：Domain 层
 * 作用：策略类只认这个接口，不认 Dubbo
 */
public interface UserGateway {

    UserInfoRpcResponse getUserByUserNo(String accountNo);

}