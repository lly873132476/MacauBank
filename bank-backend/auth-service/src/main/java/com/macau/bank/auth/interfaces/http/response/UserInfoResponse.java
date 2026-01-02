package com.macau.bank.auth.interfaces.http.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户NO
     */
    private String userNo;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 姓名
     */
    private String name;
    
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
     * 是否已设置交易密码
     */
    private Boolean hasTransactionPassword;
    
    /**
     * 创建时间
     */
    private String createTime;
}
