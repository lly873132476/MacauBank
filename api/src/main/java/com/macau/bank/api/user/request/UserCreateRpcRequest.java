package com.macau.bank.api.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 注册开户 RPC 请求
 * <p>
 * 由认证服务调用，用于在用户中心创建基础档案
 * </p>
 */
@Data
public class UserCreateRpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户编号 (Auth服务生成)", example = "U20231027001")
    private String userNo;

    @Schema(description = "用户名", example = "macau_user")
    private String userName;

    @Schema(description = "手机号", example = "66668888")
    private String mobile;

}