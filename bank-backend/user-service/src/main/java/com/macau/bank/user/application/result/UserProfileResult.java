package com.macau.bank.user.application.result;

import com.macau.bank.common.core.enums.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class UserProfileResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userNo;
    private String realNameCn;
    private String realNameEn;
    private IdCardType idCardType;
    private String idCardNo;
    private LocalDate idCardExpiry;
    private KycLevel kycLevel;
    private KycStatus kycStatus;
    private Gender gender;
    private LocalDate birthday;
    private String nationality;
    private String occupation;
    private EmploymentStatus employmentStatus;
    private String addressRegion;
    private String addressDetail;
}
