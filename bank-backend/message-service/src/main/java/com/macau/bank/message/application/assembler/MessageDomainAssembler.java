package com.macau.bank.message.application.assembler;

import com.macau.bank.message.application.result.MessageResult;
import com.macau.bank.message.domain.entity.StationMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageDomainAssembler {
    
    @Mapping(source = "msgId", target = "msgId")
    MessageResult toResult(StationMessage stationMessage);
    
    List<MessageResult> toResultList(List<StationMessage> stationMessageList);
}