package com.macau.bank.account.interfaces.http.assembler;

import com.macau.bank.account.application.query.TransactionFlowQuery;
import com.macau.bank.account.application.result.TransactionFlowResult;
import com.macau.bank.account.interfaces.http.request.TransactionFlowRequest;
import com.macau.bank.account.interfaces.http.response.TransactionFlowResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionFlowWebAssembler {

    @Mapping(target = "page", source = "pageNum")
    TransactionFlowQuery toQuery(TransactionFlowRequest request);

    @Mapping(target = "direction", source = "direction")
    @Mapping(target = "transTime", source = "transTime", qualifiedByName = "formatDateTime")
    TransactionFlowResponse toResponse(TransactionFlowResult result);

    List<TransactionFlowResponse> toResponseList(List<TransactionFlowResult> resultList);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
