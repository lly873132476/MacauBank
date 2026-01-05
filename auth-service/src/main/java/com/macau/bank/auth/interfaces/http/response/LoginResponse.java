package com.macau.bank.auth.interfaces.http.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户登录响应 VO
 * <p>
 * 包含登录成功后的关键信息，用于前端展示
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录成功响应信息")
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识 (UserNo)
     * <p>前端可用此 ID 进行后续的自动登录或数据查询</p>
     */
    @Schema(description = "用户唯一编号", example = "173849201928374")
    private String userNo;

    /**
     * 用户名
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
