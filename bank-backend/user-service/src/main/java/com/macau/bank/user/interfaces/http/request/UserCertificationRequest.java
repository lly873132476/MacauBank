package com.macau.bank.user.interfaces.http.request;

import com.macau.bank.common.core.enums.EmploymentStatus;
import com.macau.bank.common.core.enums.Gender;
import com.macau.bank.common.core.enums.IdCardType;
import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户开户认证请求
 * 承载用户提交的实名资料，包含严格的格式校验
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户开户认证请求对象")
public class UserCertificationRequest extends BaseRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "中文姓名", example = "陈大文")
    @NotBlank(message = "中文姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5.·]{2,100}$", message = "中文姓名格式不正确")
    private String realNameCn;

    @Schema(description = "英文/葡文姓名", example = "CHAN TAI MAN")
    @NotBlank(message = "英文/葡文姓名不能为空")
    @Pattern(regexp = "^[A-Za-z\\s]{2,100}$", message = "英文/葡文姓名格式不正确")
    private String realNameEn;

    @Schema(description = "证件类型: 1-澳门身份证 2-回乡证 3-护照 4-蓝卡", example = "1")
    @NotNull(message = "证件类型不能为空")
    private IdCardType idCardType;

    @Schema(description = "证件号码", example = "1234567(8)")
    @NotBlank(message = "证件号码不能为空")
    @Size(min = 5, max = 30, message = "证件号码长度非法")
    private String idCardNo;

    @Schema(description = "证件有效期")
    @NotNull(message = "证件有效期不能为空")
    @Future(message = "证件已过期")
    private Date idCardExpiry;

    @Schema(description = "发证国家/地区", example = "Macau")
    @NotBlank(message = "发证国家/地区不能为空")
    private String idCardIssueCountry;

    @Schema(description = "发证机关", example = "DSI")
    @NotBlank(message = "发证机关不能为空")
    private String idCardIssueOrg;

    @Schema(description = "证件正面照片路径", example = "/oss/idCard/front.jpg")
    @NotBlank(message = "证件正面照片不能为空")
    @Pattern(regexp = "^(http|https|/).*\\.(jpg|jpeg|png)$", message = "图片路径格式不正确")
    private String idCardImgFront;

    @Schema(description = "证件背面照片路径", example = "/oss/idCard/back.jpg")
    @NotBlank(message = "证件背面照片不能为空")
    @Pattern(regexp = "^(http|https|/).*\\.(jpg|jpeg|png)$", message = "图片路径格式不正确")
    private String idCardImgBack;

    @Schema(description = "性别 1:男 2:女", example = "1")
    @NotNull(message = "性别不能为空")
    private Gender gender;

    @Schema(description = "出生日期")
    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    private Date birthday;

    @Schema(description = "国籍/地区", example = "Macau")
    @NotBlank(message = "国籍不能为空")
    private String nationality;

    @Schema(description = "职业", example = "Engineer")
    @NotBlank(message = "职业不能为空")
    @Size(max = 100, message = "职业描述过长")
    private String occupation;

    @Schema(description = "就业状态: 1-受雇 2-自雇 3-待业 4-退休 5-学生", example = "1")
    @NotNull(message = "就业状态不能为空")
    private EmploymentStatus employmentStatus;

    @Schema(description = "税务编号 (TIN)", example = "TIN123456")
    private String taxId;

    @Schema(description = "地区 (如: 澳门半岛/氹仔/路环)", example = "氹仔")
    @NotBlank(message = "地区不能为空")
    private String addressRegion;

    @Schema(description = "详细地址", example = "某某街道某某大厦")
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "地址过长")
    private String addressDetail;
}