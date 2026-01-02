package com.macau.bank.auth.application.command;

import com.macau.bank.common.core.enums.LoginType;
import com.macau.bank.common.framework.web.model.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 用户登录指令
 * <p>
 * 承载登录业务所需的完整数据，由接口层传入
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LoginCmd extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String userName;

    /** * 密码
     * 注意：Application层通常接收的是明文或传输加密后的密码，
     * 在 Domain 层才会加密存储。
     */
    @ToString.Exclude
    private String password;

    /** *
     * clientIp
     */
    private String clientIp;

    /**
     * 登录方式
     */
    private LoginType loginType;
    
    /** 验证码 */
    private String verifyCode;
}
