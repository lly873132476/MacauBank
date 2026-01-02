package com.macau.bank.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.Deleted;
import com.macau.bank.common.core.enums.UserAuthStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户认证实体
 */
@Setter
@Getter
@ToString
@TableName("user_auth")
public class UserAuth implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户编号(系统内部唯一标识)
     */
    private String userNo;

    /**
     * 用户名(可选，部分银行APP仅允许手机号登录)
     */
    private String userName;

    /**
     * 手机区号
     */
    private String mobilePrefix;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 登录密码（加密）
     */
    private String loginPassword;

    /**
     * 交易密码（加密）
     */
    private String transactionPassword;

    /**
     * 状态 0:禁用 1:正常 2:冻结
     */
    private UserAuthStatus status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 逻辑删除 (0:未删除 1:已删除)
     */
    @TableLogic
    private Deleted deleted;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
}