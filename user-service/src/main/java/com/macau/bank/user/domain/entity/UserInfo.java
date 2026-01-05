package com.macau.bank.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.macau.bank.common.core.enums.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户详细档案实体
 */
@Getter
@Setter
@ToString
@TableName("user_info")
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联 user_auth.user_no
     */
    private String userNo;

    /**
     * 中文姓名 (如: 陈大文)
     */
    private String realNameCn;

    /**
     * 英文/葡文姓名 (如: CHAN TAI MAN)
     */
    private String realNameEn;

    /**
     * 证件类型: 1-澳门身份证(BIR) 2-回乡证 3-护照 4-蓝卡
     */
    private IdCardType idCardType;

    /**
     * 证件号码 (密文存储, 长度预留充足)
     */
    private String idCardNo;

    /**
     * 证件有效期
     */
    private LocalDate idCardExpiry;

    /**
     * 发证国家/地区
     * 默认: Macau
     */
    private String idCardIssueCountry;

    /**
     * 发证机关
     * 默认: DSI
     */
    private String idCardIssueOrg;

    /**
     * 证件正面图片路径
     */
    private String idCardImgFront;

    /**
     * 证件背面图片路径
     */
    private String idCardImgBack;

    /**
     * 认证等级: 0-匿名 1-初级(L1) 2-高级(L2)
     */
    private KycLevel kycLevel;

    /**
     * 认证状态: 0-未认证 1-审核中 2-审核通过 3-审核驳回
     */
    private KycStatus kycStatus;

    /**
     * 客户等级: NORMAL-普通, GOLD-黄金, DIAMOND-钻石
     */
    private UserLevel userLevel;

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
     * 默认: Macau
     */
    private String nationality;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 就业状态: 1-受雇 2-自雇 3-待业 4-退休 5-学生
     */
    private EmploymentStatus employmentStatus;

    /**
     * 税务编号 (TIN)
     */
    private String taxId;

    /**
     * 地区 (如: 澳门半岛/氹仔/路环)
     */
    private String addressRegion;

    /**
     * 详细地址
     */
    private String addressDetail;

    /**
     * 逻辑删除: 0-未删除 1-已删除
     */
    @TableLogic
    private Deleted deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}