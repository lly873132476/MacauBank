package com.macau.bank.auth.interfaces.http.request;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

import static com.macau.bank.common.core.constant.RegexPatterns.PASSWORD_COMPLEX;

/**
 * 修改密码请求DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户修改密码")
public class UpdatePasswordRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码(8-20位)", example = "Macau@2025")
    private String oldPassword;
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = PASSWORD_COMPLEX, message = "密码需包含字母和数字，长度8-20位")
    @Schema(description = "新密码(8-20位)", example = "Macau@2026")
    private String newPassword;
}
