package com.macau.bank.message.interfaces.http.assembler;

import com.macau.bank.message.application.result.MessageResult;
import com.macau.bank.message.interfaces.http.response.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageWebAssembler {

    @Mapping(source = "msgId", target = "messageId")
    @Mapping(source = "category", target = "type")
    @Mapping(source = "createTime", target = "createTime", qualifiedByName = "formatDateTime")
    MessageResponse toResponse(MessageResult result);

    List<MessageResponse> toResponseList(List<MessageResult> resultList);

    @Named("formatDateTime")
    default String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}