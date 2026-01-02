package com.macau.bank.user.interfaces.http.assembler;

import com.macau.bank.user.application.command.UpdateProfileCmd;
import com.macau.bank.user.application.command.UserCertificationCmd;
import com.macau.bank.user.application.result.UserProfileResult;
import com.macau.bank.user.interfaces.http.request.UpdateProfileRequest;
import com.macau.bank.user.interfaces.http.request.UserCertificationRequest;
import com.macau.bank.user.interfaces.http.response.UserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserWebAssembler {

    UpdateProfileCmd toCmd(UpdateProfileRequest request);

    UserCertificationCmd toCmd(UserCertificationRequest request);

    @Mapping(target = "idCardNo", source = "idCardNo", qualifiedByName = "maskIdCard")
    @Mapping(target = "idCardExpiry", source = "idCardExpiry", qualifiedByName = "formatDate")
    @Mapping(target = "birthday", source = "birthday", qualifiedByName = "formatDate")
    UserProfileResponse toResponse(UserProfileResult result);

    @Named("maskIdCard")
    default String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 4) return idCard;
        return idCard.substring(0, 3) + "****" + idCard.substring(idCard.length() - 2);
    }

    @Named("formatDate")
    default String formatDate(java.time.LocalDate date) {
        if (date == null) return null;
        return date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}