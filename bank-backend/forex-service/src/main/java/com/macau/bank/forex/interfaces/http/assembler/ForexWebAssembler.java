package com.macau.bank.forex.interfaces.http.assembler;

import com.macau.bank.forex.application.command.ExchangeCmd;
import com.macau.bank.forex.application.result.ExchangeResult;
import com.macau.bank.forex.interfaces.http.request.ForexExchangeRequest;
import com.macau.bank.forex.interfaces.http.response.ForexExchangeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface ForexWebAssembler {

    @Mapping(target = "userNo", ignore = true)
    @Mapping(target = "requestId", source = "requestId")
    ExchangeCmd toCmd(ForexExchangeRequest request);

    @Mapping(target = "transTime", source = "transTime", qualifiedByName = "formatDateTime")
    ForexExchangeResponse toResponse(ExchangeResult result);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
