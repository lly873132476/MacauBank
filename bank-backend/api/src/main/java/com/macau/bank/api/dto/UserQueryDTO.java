package com.macau.bank.api.dto;

import com.macau.bank.common.core.enums.Gender;
import com.macau.bank.common.core.enums.IdCardType;
import com.macau.bank.common.core.enums.KycLevel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户查询条件DTO
 * 用于封装用户查询条件
 */
@Data
public class UserQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联 user_auth.user_no
     */
    private String userNo;

    /**
     * 中文姓名
     */
    private String realNameCn;

    /**
     * 英文/葡文姓名
     */
    private String realNameEn;

    /**
     * 证件类型: 1-澳门身份证(BIR) 2-回乡证 3-护照 4-蓝卡
     */
    private IdCardType idCardType;

    /**
     * 证件号码
     */
    private String idCardNo;

    /**
     * 证件有效期
     */
    private LocalDate idCardExpiry;

    /**
     * 认证等级: 0-未认证 1-初级认证 2-柜台面签
     */
    private KycLevel kycLevel;

    /**
     * 性别 0:未知 1:男 2:女
     */
    private Gender gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 国籍/地区
     */
    private String nationality;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 地区 (如: 澳门半岛/氹仔/路环)
     */
    private String addressRegion;

    /**
     * 详细地址 (街道/大厦/单位)
     */
    private String addressDetail;

}