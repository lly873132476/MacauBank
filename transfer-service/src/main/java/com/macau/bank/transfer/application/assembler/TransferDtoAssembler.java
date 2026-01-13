package com.macau.bank.transfer.application.assembler;

import com.macau.bank.transfer.application.result.TransferOrderResult;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 转账 DTO 装配器
 * <p>
 * 职责：负责 Application 层与 Domain 层、外部接口之间的对象转换
 * 位置：Application 层（符合 DDD 分层规范）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransferDtoAssembler {

    @Mapping(source = "payeeInfo.accountNo", target = "payeeAccountNo")
    @Mapping(source = "amount.amount", target = "amount")
    @Mapping(source = "amount.currencyCode", target = "currencyCode")
    TransferOrderResult toResult(TransferOrder transferOrder);

    List<TransferOrderResult> toResultList(List<TransferOrder> transferOrderList);
}
