package com.macau.bank.transfer.interfaces.http.assembler;

import com.macau.bank.api.dto.TransferRequest;
import com.macau.bank.transfer.application.command.AddPayeeCmd;
import com.macau.bank.transfer.application.command.TransferCmd;
import com.macau.bank.transfer.application.command.UpdatePayeeCmd;
import com.macau.bank.transfer.application.result.PayeeResult;
import com.macau.bank.transfer.application.result.TransferOrderResult;
import com.macau.bank.transfer.application.result.TransferResult;
import com.macau.bank.transfer.interfaces.http.request.AddPayeeRequest;
import com.macau.bank.transfer.interfaces.http.request.UpdatePayeeRequest;
import com.macau.bank.transfer.interfaces.http.response.PayeeResponse;
import com.macau.bank.transfer.interfaces.http.response.TransferOrderResponse;
import com.macau.bank.transfer.interfaces.http.response.TransferResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferWebAssembler {

    AddPayeeCmd toCmd(AddPayeeRequest request);

    UpdatePayeeCmd toCmd(UpdatePayeeRequest request);

    TransferCmd toCmd(TransferRequest request);

    @Mapping(target = "lastTransTime", source = "lastTransTime", qualifiedByName = "formatDateTime")
    PayeeResponse toResponse(PayeeResult result);

    List<PayeeResponse> toPayeeResponseList(List<PayeeResult> resultList);

    /**
     * 转换转账结果为响应对象
     * <p>
     * 注意：TransferResult 仅包含核心字段（txnId, status, message, createTime），
     * 其他字段（transactionId, fromAccountNo, toAccountNo, amount, currencyCode, fee,
     * transferType）
     * 在源对象中不存在，因此忽略映射
     */
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "fromAccountNo", ignore = true)
    @Mapping(target = "toAccountNo", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "currencyCode", ignore = true)
    @Mapping(target = "fee", ignore = true)
    @Mapping(target = "transferType", ignore = true)
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "formatDateTime")
    TransferResponse toResponse(TransferResult result);

    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "formatDateTime")
    TransferOrderResponse toResponse(TransferOrderResult result);

    List<TransferOrderResponse> toResponseList(List<TransferOrderResult> resultList);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return null;
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}