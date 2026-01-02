package com.macau.bank.auth.interfaces.http.request;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

import static com.macau.bank.common.core.constant.RegexPatterns.MOBILE;
import static com.macau.bank.common.core.constant.RegexPatterns.PASSWORD_COMPLEX;

/**
 * 用户注册请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户注册提交表单")
public class RegisterRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户名 (可选，若不填则由系统自动生成) */
    @Schema(description = "用户名 (4-30位)", example = "macau_user_01")
    private String userName;

    /** 验证码 */
    @Schema(description = "短信验证码", example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    /** 密码 */
    @Schema(description = "密码 (8-20位，含字母数字)", example = "Macau@2025")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = PASSWORD_COMPLEX, message = "密码需包含字母和数字，长度8-20位")
    @ToString.Exclude // 安全关键：禁止打印日志
    private String password;

    /** 手机区号 */
    @Schema(description = "手机区号", example = "+853")
    private String mobilePrefix;

    /** 手机号 */
    @Schema(description = "手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = MOBILE, message = "手机号格式不正确")
    private String mobile;

}