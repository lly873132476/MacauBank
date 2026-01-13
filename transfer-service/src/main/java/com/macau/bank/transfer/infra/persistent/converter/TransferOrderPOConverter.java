package com.macau.bank.transfer.infra.persistent.converter;

import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.infra.persistent.po.TransferOrderPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 转账订单对象转换器
 * <p>
 * 负责 Entity <-> PO 的互相转换
 */
@Mapper(componentModel = "spring")
public interface TransferOrderPOConverter {

    @Mapping(target = "payerInfo", expression = "java(PayerInfo.of(po.getUserNo(), po.getPayerAccountNo(), po.getPayerAccountName(), po.getPayerCurrency()))")
    @Mapping(target = "payeeInfo", expression = "java(PayeeInfo.builder().accountNo(po.getPayeeAccountNo()).accountName(po.getPayeeAccountName()).bankCode(po.getPayeeBankCode()).swiftCode(po.getPayeeSwiftCode()).fpsId(po.getPayeeFpsId()).build())")
    @Mapping(target = "amount", expression = "java(Money.of(po.getAmount(), po.getCurrencyCode()))")
    TransferOrder toEntity(TransferOrderPO po);

    @Mapping(source = "payerInfo.userNo", target = "userNo")
    @Mapping(source = "payerInfo.accountNo", target = "payerAccountNo")
    @Mapping(source = "payerInfo.accountName", target = "payerAccountName")
    @Mapping(source = "payerInfo.currency", target = "payerCurrency")
    @Mapping(source = "payeeInfo.accountNo", target = "payeeAccountNo")
    @Mapping(source = "payeeInfo.accountName", target = "payeeAccountName")
    @Mapping(source = "payeeInfo.bankCode", target = "payeeBankCode")
    @Mapping(source = "payeeInfo.swiftCode", target = "payeeSwiftCode")
    @Mapping(source = "payeeInfo.fpsId", target = "payeeFpsId")
    @Mapping(source = "amount.amount", target = "amount")
    @Mapping(source = "amount.currencyCode", target = "currencyCode")
    TransferOrderPO toPO(TransferOrder entity);
}
