package com.macau.bank.account.application.assembler;

import com.macau.bank.account.application.command.AdjustBalanceCmd;
import com.macau.bank.account.application.result.AccountBalanceResult;
import com.macau.bank.account.application.result.AccountInfoResult;
import com.macau.bank.account.domain.entity.AccountBalance;
import com.macau.bank.account.domain.entity.AccountInfo;
import com.macau.bank.account.domain.model.BalanceAdjustment;
import com.macau.bank.common.core.domain.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountDomainAssembler {

    // ==========================================
    // ⬆️ 出参转换：Entity -> Result
    // ==========================================
    AccountInfoResult toResult(AccountInfo accountInfo);

    AccountBalanceResult toResult(AccountBalance accountBalance);

    List<AccountInfoResult> toInfoList(List<AccountInfo> accountInfoList);

    List<AccountBalanceResult> toBalanceList(List<AccountBalance> accountBalanceList);

    // ==========================================
    // ⬇️ 入参转换：Cmd -> Domain ValueObject
    // ==========================================
    @Mapping(target = "amount", source = "cmd", qualifiedByName = "toMoney")
    BalanceAdjustment toBalanceAdjustment(AdjustBalanceCmd cmd);

    @Named("toMoney")
    default Money toMoney(AdjustBalanceCmd cmd) {
        return Money.of(cmd.getAmount(), cmd.getCurrencyCode());
    }
}
