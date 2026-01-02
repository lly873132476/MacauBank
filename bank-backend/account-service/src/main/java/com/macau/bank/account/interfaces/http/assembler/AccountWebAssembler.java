package com.macau.bank.account.interfaces.http.assembler;

import com.macau.bank.account.application.command.AdminDepositCmd;
import com.macau.bank.account.application.command.AdminOpenCurrencyAccountCmd;
import com.macau.bank.account.application.result.AccountBalanceResult;
import com.macau.bank.account.application.result.AccountInfoResult;
import com.macau.bank.account.application.result.AssetSummaryResult;
import com.macau.bank.account.interfaces.http.dto.AdminDepositRequest;
import com.macau.bank.account.interfaces.http.dto.AdminOpenCurrencyRequest;
import com.macau.bank.account.interfaces.http.response.AccountBalanceResponse;
import com.macau.bank.account.interfaces.http.response.AccountResponse;
import com.macau.bank.account.interfaces.http.response.AssetSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountWebAssembler {
    
    // Request -> Cmd
    AdminDepositCmd toCmd(AdminDepositRequest request);

    AdminOpenCurrencyAccountCmd toCmd(AdminOpenCurrencyRequest request);

    // Result -> Response
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "formatDateTime")
    AccountResponse toResponse(AccountInfoResult result);

    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "formatDateTime")
    AccountBalanceResponse toResponse(AccountBalanceResult result);

    AssetSummaryResponse toResponse(AssetSummaryResult result);

    List<AccountResponse> toResponseList(List<AccountInfoResult> resultList);

    @Named("formatDateTime")
    default String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
