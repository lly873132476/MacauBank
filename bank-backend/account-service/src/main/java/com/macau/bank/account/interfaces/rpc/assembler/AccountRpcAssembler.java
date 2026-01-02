package com.macau.bank.account.interfaces.rpc.assembler;

import com.macau.bank.account.application.command.AdjustBalanceCmd;
import com.macau.bank.account.application.command.CreateAccountCmd;
import com.macau.bank.account.application.command.CreditCmd;
import com.macau.bank.account.application.command.DebitCmd;
import com.macau.bank.account.application.command.FreezeBalanceCmd;
import com.macau.bank.account.application.command.UnfreezeAndDebitCmd;
import com.macau.bank.account.application.command.UnfreezeBalanceCmd;
import com.macau.bank.account.application.result.AccountBalanceResult;
import com.macau.bank.account.application.result.AccountInfoResult;
import com.macau.bank.account.application.result.AssetSummaryResult;
import com.macau.bank.api.account.request.AdjustBalanceRpcRequest;
import com.macau.bank.api.account.request.CreateAccountRpcRequest;
import com.macau.bank.api.account.request.CreditRpcRequest;
import com.macau.bank.api.account.request.DebitRpcRequest;
import com.macau.bank.api.account.request.FreezeBalanceRpcRequest;
import com.macau.bank.api.account.request.UnfreezeAndDebitRpcRequest;
import com.macau.bank.api.account.request.UnfreezeBalanceRpcRequest;
import com.macau.bank.api.account.response.AccountBalanceRpcResponse;
import com.macau.bank.api.account.response.AccountInfoRpcResponse;
import com.macau.bank.api.account.response.AssetSummaryRpcResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountRpcAssembler {

    // ==================== Result -> RPC Response ====================

    AccountInfoRpcResponse toRpc(AccountInfoResult result);

    AccountBalanceRpcResponse toRpc(AccountBalanceResult result);

    AssetSummaryRpcResponse toRpc(AssetSummaryResult result);

    List<AccountInfoRpcResponse> toRpcList(List<AccountInfoResult> resultList);

    // ==================== RPC Request -> Cmd ====================

    AdjustBalanceCmd toCmd(AdjustBalanceRpcRequest request);

    DebitCmd toCmd(DebitRpcRequest request);

    CreditCmd toCmd(CreditRpcRequest request);

    CreateAccountCmd toCmd(CreateAccountRpcRequest request);

    FreezeBalanceCmd toCmd(FreezeBalanceRpcRequest request);

    UnfreezeBalanceCmd toCmd(UnfreezeBalanceRpcRequest request);

    UnfreezeAndDebitCmd toCmd(UnfreezeAndDebitRpcRequest request);
}
