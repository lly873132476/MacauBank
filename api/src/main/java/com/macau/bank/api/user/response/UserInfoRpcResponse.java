package com.macau.bank.api.user.response;

import com.macau.bank.common.core.enums.UserLevel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息 RpcDTO
 */
@Data
public class UserInfoRpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户编号
     */
    private String userNo;

    /**
     * 用户等级 (用于限额与算费)
     */
    private UserLevel userLevel;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 中文姓名 (如: 陈大文)
     */
    private String realNameCn;

    /**
     * 英文/葡文姓名 (如: CHAN TAI MAN)
     */
    private String realNameEn;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}