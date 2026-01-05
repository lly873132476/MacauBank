package com.macau.bank.auth.interfaces.http.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户注册响应 VO
 * <p>
 * 包含注册成功后的关键信息，用于前端展示或自动登录
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "注册成功响应信息")
public class RegisterResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识 (UserNo)
     * <p>前端可用此 ID 进行后续的自动登录或数据查询</p>
     */
    @Schema(description = "用户唯一编号", example = "173849201928374")
    private String userNo;

//    /**
//     * 初始生成的银行账户号
//     * <p>注意：此处返回的是完整账号，仅用于注册成功页面的首次展示</p>
//     */
//    @Schema(description = "初始银行账户", example = "6222021001122334455")
//    private String accountNo;

    /**
     * 用户名 (登录账号)
     */
    @Schema(description = "用户名", example = "macau_user_01")
    private String userName;

    /**
     * token
     * <p>注意：用于获取用户登录信息</p>
     */
    @Schema(description = "token", example = "auth:token:eyJhbGciOiJIUzI1NiIsInR5c")
    private String token;
}
