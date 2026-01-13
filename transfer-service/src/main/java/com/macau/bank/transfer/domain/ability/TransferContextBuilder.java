package com.macau.bank.transfer.domain.ability;

import com.macau.bank.api.user.response.UserInfoRpcResponse;
import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.TransferChannel;
import com.macau.bank.common.core.enums.UserLevel;
import com.macau.bank.transfer.application.command.TransferCmd;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.gateway.UserGateway;
import com.macau.bank.transfer.domain.model.AccountSnapshot;
import com.macau.bank.transfer.domain.service.TransferFeeDomainService;
import com.macau.bank.transfer.domain.service.TransferOrderDomainService;
import com.macau.bank.transfer.domain.valobj.PayeeInfo;
import com.macau.bank.transfer.domain.valobj.PayerInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferContextBuilder {

    @Resource
    private TransferFeeDomainService feeService;

    @Resource
    private TransferOrderDomainService orderDomainService;

    @Resource
    private AccountGateway accountGateway;

    @Resource
    private UserGateway userGateway;

    public TransferContext build(TransferCmd cmd) {
        TransferContext context = new TransferContext();

        // 1. 直接构建 Order 对象 (内存态)
        TransferOrder order = new TransferOrder();

        // 构建值对象
        order.setPayerInfo(PayerInfo.of(
                null, // userNo 暂未获取
                cmd.getFromAccountNo(),
                null, // accountName 暂未获取
                cmd.getCurrencyCode()));

        order.setPayeeInfo(PayeeInfo.builder()
                .accountNo(cmd.getToAccountNo())
                .accountName(cmd.getToAccountName())
                .bankCode(cmd.getToBankCode())
                .swiftCode(cmd.getSwiftCode())
                .fpsId(cmd.getFpsId())
                .build());

        order.setAmount(Money.of(cmd.getAmount(), cmd.getCurrencyCode()));

        order.setFeeType(cmd.getFeeType());
        order.setIdempotentKey(cmd.getIdempotentKey());
        order.setTransferType(cmd.getTransferType());
        order.setRemark(cmd.getRemark());

        // 2. 放入 Context
        context.setOrder(order);

        // 3. 设置 Context 特有的过程数据
        context.setTransactionPassword(cmd.getTransactionPassword());

        return context;
    }

    /**
     * 上下文补全：查 RPC -> 算费 -> 填充 Context
     */
    public void enrich(TransferContext context, TransferChannel channel) {
        TransferOrder order = context.getOrder();

        // 1. 查付款账户 (并行查询优化点)
        AccountSnapshot fromAccount = accountGateway.getAccount(order.getPayerInfo().getAccountNo());

        // 2. 查用户等级
        UserInfoRpcResponse user = userGateway.getUserByUserNo(fromAccount.getUserNo());
        UserLevel level = (user != null) ? user.getUserLevel() : UserLevel.NORMAL;

        // 3. 算费
        BigDecimal fee = feeService.calculateFee(channel.name(), order.getPayerInfo().getCurrency(), level.getCode(),
                context.getAmount());

        // 4. 填充数据
        order.setPayerInfo(order.getPayerInfo().toBuilder()
                .userNo(fromAccount.getUserNo())
                .accountName(fromAccount.getAccountName())
                .build());
        order.setFee(fee);
        order.setTransferChannel(channel);
        context.setPayerUserLevel(level);
        context.setPayerAccount(fromAccount);

    }

    public TransferContext rebuild(String txnId) {

        TransferOrder order = orderDomainService.getTransferOrderByTxnId(txnId);
        TransferContext context = TransferContext
                .builder()
                .order(order)
                .build();

        return context;
    }
}