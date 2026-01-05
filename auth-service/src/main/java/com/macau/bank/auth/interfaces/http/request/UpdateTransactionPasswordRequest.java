package com.macau.bank.auth.interfaces.http.request;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 设置交易密码请求DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户修改密码")
public class UpdateTransactionPasswordRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 交易密码(6位数字)
     */
    @NotBlank(message = "交易密码不能为空")
    @Pattern(regexp = "^[0-9]\\d{6}$", message = "交易密码为纯数字，长度6位")
    @Schema(description = "交易密码(6位纯数字)", example = "123456")
    private String password;
}
