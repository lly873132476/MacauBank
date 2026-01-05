package com.macau.bank.user.application.command;

import com.macau.bank.common.core.enums.EmploymentStatus;
import com.macau.bank.common.core.enums.Gender;
import com.macau.bank.common.core.enums.IdCardType;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户认证指令
 */
@Data
public class UserCertificationCmd implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userNo;
    private String realNameCn;
    private String realNameEn;
    private IdCardType idCardType;
    private String idCardNo;
    private Date idCardExpiry;
    private String idCardIssueCountry;
    private String idCardIssueOrg;
    private String idCardImgFront;
    private String idCardImgBack;
    private Gender gender;
    private Date birthday;
    private String nationality;
    private String occupation;
    private EmploymentStatus employmentStatus;
    private String taxId;
    private String addressRegion;
    private String addressDetail;
}
