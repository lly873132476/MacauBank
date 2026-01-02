package com.macau.bank.auth.application.result;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录业务结果
 * <p>
 * 返回核心业务标识，供上层（Controller/RPC）封装
 * </p>
 */
@Data
@Builder
public class LoginResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识 (UserNo)
     * <p>前端可用此 ID 进行后续的自动登录或数据查询</p>
     */
    private String userNo;

    /**
     * 用户名
     */
    private String userName;

    /**
     * token
     * <p>注意：用于获取用户登录信息</p>
     */
    private String token;
}