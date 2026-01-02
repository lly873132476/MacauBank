package com.macau.bank.user.application.assembler;

import com.macau.bank.user.application.command.UserCertificationCmd;
import com.macau.bank.user.application.result.UserProfileResult;
import com.macau.bank.user.domain.entity.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserDomainAssembler {

    UserInfo toEntity(UserCertificationCmd cmd);

    UserProfileResult toResult(UserInfo userInfo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "kycLevel", ignore = true)
    @Mapping(target = "kycStatus", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(UserInfo source, @MappingTarget UserInfo target);
}