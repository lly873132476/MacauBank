package com.macau.bank.user.interfaces.http.response;

import com.macau.bank.common.core.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 用户个人信息详情响应对象
 * <p>
 * 包含用户实名认证信息、KYC状态及联系方式
 * </p>
 */
@Data
@Schema(description = "用户个人信息详情响应对象")
public class UserProfileResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "中文姓名", example = "陈大文")
    private String realNameCn;

    @Schema(description = "英文/葡文姓名", example = "CHAN TAI MAN")
    private String realNameEn;

    @Schema(description = "证件类型", example = "1")
    private IdCardType idCardType;

    @Schema(description = "证件号码 (脱敏显示)", example = "123****8")
    private String idCardNo;

    @Schema(description = "证件有效期", example = "2030-12-31")
    private String idCardExpiry;

    @Schema(description = "KYC等级", example = "L2")
    private KycLevel kycLevel;

    @Schema(description = "KYC状态", example = "2")
    private KycStatus kycStatus;

    @Schema(description = "性别 1:男 2:女", example = "1")
    private Gender gender;

    @Schema(description = "出生日期", example = "1990-01-01")
    private String birthday;

    @Schema(description = "国籍", example = "Macau")
    private String nationality;

    @Schema(description = "职业", example = "Engineer")
    private String occupation;

    @Schema(description = "就业状态", example = "1")
    private EmploymentStatus employmentStatus;

    @Schema(description = "地区", example = "氹仔")
    private String addressRegion;

    @Schema(description = "详细地址", example = "某某花园A座10楼")
    private String addressDetail;
}