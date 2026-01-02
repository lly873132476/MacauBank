package com.macau.bank.auth.interfaces.http.request;

import com.macau.bank.common.core.enums.LoginType;
import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 登录请求Request
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户登录请求参数")
public class LoginRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名或手机号", example = "macau_user_01")
    @NotBlank(message = "账号不能为空")
    private String userName;

    private LoginType loginType = LoginType.PASSWORD;

    @Schema(description = "密码 (loginType=PASSWORD时必填)", example = "Macau@2025")
    private String password;
    
    @Schema(description = "验证码 (loginType=SMS时必填)", example = "123456")
    private String verifyCode;

}
