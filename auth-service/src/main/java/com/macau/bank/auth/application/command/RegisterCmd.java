package com.macau.bank.auth.application.command;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 用户注册指令
 * <p>
 * 承载注册业务所需的完整数据，由接口层传入
 * </p>
 */
@Data
public class RegisterCmd implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String userName;

    /** * 密码
     * 注意：Application层通常接收的是明文或传输加密后的密码，
     * 在 Domain 层才会加密存储。
     */
    @ToString.Exclude // 【关键】防止日志泄露
    private String password;

    /** 手机区号 */
    private String mobilePrefix;

    /** 手机号 */
    private String mobile;

    /** 验证码 */
    private String verifyCode;

}