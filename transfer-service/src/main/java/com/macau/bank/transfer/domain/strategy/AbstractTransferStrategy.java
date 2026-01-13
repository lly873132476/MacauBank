package com.macau.bank.transfer.domain.strategy;

import com.macau.bank.common.core.enums.TransferChannel;
import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.transfer.domain.ability.TransferContextBuilder;
import com.macau.bank.transfer.domain.ability.TransferValidator;
import com.macau.bank.transfer.domain.context.TransferContext;
import com.macau.bank.transfer.domain.gateway.AccountGateway;
import com.macau.bank.transfer.domain.service.TransferOrderDomainService;
import com.macau.bank.transfer.domain.statemachine.StateMachineExecutor;
import com.macau.bank.transfer.domain.statemachine.StateTransition;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.List;

/**
 * 转账策略抽象基类 (模板方法模式)
 * <p>
 * 封装了转账的标准流程：数据准备 -> 统一校验 -> 子类校验 -> 事务执行 -> 后置处理
 */
public abstract class AbstractTransferStrategy implements TransferStrategy {

    @Resource
    protected TransferContextBuilder contextBuilder;

    @Resource
    protected TransferValidator validator;

    @Resource
    protected TransferOrderDomainService orderDomainService;

    @Resource
    protected AccountGateway accountGateway;

    @Resource
    protected StateMachineExecutor stateMachineExecutor;

    /**
     * 执行转账（Template Method）
     * 注意：此方法负责前置准备和发消息，不涉及数据库强事务，因此不需要 @GlobalTransactional
     * 避免 RPC (enrich) 拉长事务生命周期
     */
    @Override
    public TransferContext execute(TransferContext context) {
        // --- 1. 公共准备阶段 (这一步所有策略都一样) ---
        // 补全数据 (查户口)
        contextBuilder.enrich(context, getTransferChannel(context));

        // 统一校验 (查余额、限额)
        validator.validate(context);

        // 交给子类的补充
        doCustomPrepareAndValidate(context);

        // --- 2. 启动阶段 ---
        // 先落库 (状态为 INIT)
        orderDomainService.createOrder(context);

        // --- 3. 启动状态机 ---
        // 获取当前策略类流程配置，自动开始跑第一阶段
        StateTransition transition = this.getNextTransition(TransferStatus.INIT, true);
        stateMachineExecutor.drive(context, transition);

        // --- 4. 返回上下文（由 Application 层组装 TransferResult）---
        return context;
    }

    /**
     * 校验实现：复用 validator
     */
    @Override
    public boolean validate(TransferContext context) {
        try {
            validator.validate(context);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 算费实现：复用 assembler/feeService
     */
    @Override
    public BigDecimal calculateFee(TransferContext context) {
        return context.getOrder().getFee();
    }

    /**
     * 钩子方法：子类自定义校验逻辑
     */
    protected void doCustomPrepareAndValidate(TransferContext context) {
        // 默认不操作，子类按需重写
    }

    /**
     * 【核心优化】由子类决定：在当前状态下，应该执行哪些 Handler？
     * 下一步要去哪个状态？
     */
    @Override
    public abstract StateTransition getNextTransition(TransferStatus currentStatus, boolean isRiskPass);

    /**
     * 获取支持的转账渠道
     */
    @Override
    public abstract TransferChannel getTransferChannel(TransferContext context);

    /**
     * 获取冲正流程配置（默认实现）
     * <p>
     * 基于订单原状态返回通用冲正路径。
     * 子类可以重写以提供特定策略的冲正逻辑（如跨境转账需要额外通知 SWIFT）。
     */
    @Override
    public StateTransition getReversalTransition(TransferStatus originalStatus) {
        switch (originalStatus) {
            case SUCCESS:
                // 成功的订单：完整冲正
                return new StateTransition(
                        java.util.List.of(
                                TransferPhaseEnum.REVERSE_CREDIT,
                                TransferPhaseEnum.REVERSE_DEDUCT,
                                TransferPhaseEnum.REVERSE_FEE),
                        TransferStatus.REVERSED);

            case FAILED:
            case PENDING_COMPENSATION:
            case INIT:
            case PENDING_RISK:
                // 失败/挂起/初始化/风控中的订单：仅解冻
                return new StateTransition(
                        java.util.List.of(TransferPhaseEnum.UNFREEZE),
                        TransferStatus.REVERSED);

            default:
                return null;
        }
    }
}