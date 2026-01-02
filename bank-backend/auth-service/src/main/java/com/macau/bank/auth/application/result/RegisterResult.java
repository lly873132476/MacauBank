package com.macau.bank.auth.application.result;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册业务结果
 * <p>
 * 返回核心业务标识，供上层（Controller/RPC）封装
 * </p>
 */
@Data
@Builder
public class RegisterResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识 (UserNo)
     */
    private String userNo;

//    /**
//     * 初始生成的银行账户号
//     */
//    private String accountNo;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 登录令牌
     */
    private String token;
}