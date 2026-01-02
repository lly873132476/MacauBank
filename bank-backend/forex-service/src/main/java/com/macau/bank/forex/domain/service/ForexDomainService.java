package com.macau.bank.forex.domain.service;

import com.macau.bank.common.core.enums.CommonStatus;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.util.IdGenerator;
import com.macau.bank.forex.common.enums.ForexDirectionEnum;
import com.macau.bank.forex.common.enums.ForexTradeStatusEnum;
import com.macau.bank.forex.common.enums.PositionStatus;
import com.macau.bank.forex.domain.entity.BankPosition;
import com.macau.bank.forex.domain.entity.CurrencyPairConfig;
import com.macau.bank.forex.domain.entity.ForexQuoteLog;
import com.macau.bank.forex.domain.entity.ForexTradeOrder;
import com.macau.bank.forex.domain.repository.BankPositionRepository;
import com.macau.bank.forex.domain.repository.CurrencyPairConfigRepository;
import com.macau.bank.forex.domain.repository.ForexQuoteLogRepository;
import com.macau.bank.forex.domain.repository.ForexTradeOrderRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ForexDomainService {

    @Resource
    private ForexTradeOrderRepository forexTradeOrderRepository;

    @Resource
    private BankPositionRepository bankPositionRepository;

    @Resource
    private ForexQuoteLogRepository forexQuoteLogRepository;

    @Resource
    private CurrencyPairConfigRepository currencyPairConfigRepository;

    /**
     * 校验请求是否重复 (幂等性检查)
     * @param requestId 幂等请求ID
     * @return 如果存在返回已有订单，否则返回 null
     */
    public ForexTradeOrder findByRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return null;
        }
        return forexTradeOrderRepository.findByRequestId(requestId);
    }

    /**
     * 校验并获取配置 (App层调用此方法获取配置，或者App层直接查Mapper也可以)
     */
    public CurrencyPairConfig getAndValidateConfig(String pairCode) {
        CurrencyPairConfig config = currencyPairConfigRepository.findByPairCode(pairCode);
        if (config == null || config.getStatus() == CommonStatus.DISABLED) {
            throw new BusinessException("E001", "交易对不存在或已停盘: " + pairCode);
        }
        return config;
    }

    @Transactional
    public ForexTradeOrder createOrder(String requestId,
                                       String userNo,
                                       String pairCode,
                                       String sellCurrency,
                                       BigDecimal sellAmount,
                                       String buyCurrency, // 需要它来判断买卖方向
                                       CurrencyPairConfig pairConfig,
                                       BigDecimal marketRate,
                                       BigDecimal quoteToMopRate) {
        // 交易方向判断逻辑
        ForexDirectionEnum direction;
        if (buyCurrency.equals(pairConfig.getBaseCurrency())) {
            direction = ForexDirectionEnum.BUY; // 用户在买入基准
        } else if (sellCurrency.equals(pairConfig.getBaseCurrency())) {
            direction = ForexDirectionEnum.SELL; // 用户在卖出基准
        } else {
            throw new BusinessException("E002", "币种与交易对不匹配");
        }

        // 模拟成交价
        BigDecimal dealRate = calculateDealRate(direction, marketRate);

        // ==========================================
        //          计算银行点差利润 (MOP)
        // ==========================================

        // 1. 确定交易的基础货币数量 (Base Amount)
        // 无论买还是卖，汇率都是针对 Base Currency 的 (如 1 USD = ? MOP)
        BigDecimal buyAmount;
        BigDecimal baseAmount;
        if (sellCurrency.equals(pairConfig.getBaseCurrency())) {
            baseAmount = sellAmount; // 用户卖出 Base (银行买入 Base)
            buyAmount = sellAmount.multiply(dealRate).setScale(2, RoundingMode.DOWN);
        } else {
            buyAmount = sellAmount.divide(dealRate,2, RoundingMode.DOWN);
            baseAmount = buyAmount; // 用户买入 Base (银行卖出 Base)
        }

        // 2. 计算原始利润 (单位：Quote Currency)
        // 公式：Base数量 * |成交价 - 市场成本价|
        BigDecimal rateDiff = dealRate.subtract(marketRate).abs();
        BigDecimal profitInQuote = baseAmount.multiply(rateDiff);

        // 3. 折算成 MOP
        // 如果 Quote 就是 MOP，汇率应该是 1；如果不是，乘以前端传进来的汇率
        BigDecimal profitMop = profitInQuote.multiply(quoteToMopRate)
                .setScale(2, RoundingMode.HALF_UP);

        ForexTradeOrder order = new ForexTradeOrder();
        order.setTxnId("FX" + IdGenerator.generateId());
        order.setRequestId(requestId);
        order.setUserNo(userNo);
        order.setPairCode(pairCode);
        order.setDirection(direction);
        order.setSellCurrency(sellCurrency);
        order.setSellAmount(sellAmount);
        order.setBuyCurrency(buyCurrency);
        order.setBuyAmount(buyAmount);
        order.setMarketRate(marketRate);
        order.setDealRate(dealRate);

        order.setProfitAmountMop(profitMop);

        order.setStatus(ForexTradeStatusEnum.PROCESSING);
        order.setRiskCheckPass(true);
        order.setCreateTime(LocalDateTime.now());

        forexTradeOrderRepository.save(order);

        // 记录报价审计日志 (确保每一笔交易有据可查)
        recordQuoteLog(order.getTxnId(), order.getPairCode(),
                marketRate, marketRate, marketRate); // 暂取中间价，实际应记录Ask/Bid

        return order;
    }

    public void updateOrderStatus(String txnId, ForexTradeStatusEnum status, String failReason) {
        forexTradeOrderRepository.updateStatus(txnId, status, failReason);
    }

    /**
     * 记录报价日志 (审计)
     */
    public void recordQuoteLog(String txnId, String pairCode, BigDecimal ask, BigDecimal bid, BigDecimal mid) {
        ForexQuoteLog log = new ForexQuoteLog();
        log.setTxnId(txnId);
        log.setPairCode(pairCode);
        log.setQuoteTime(LocalDateTime.now());
        log.setAskPrice(ask);
        log.setBidPrice(bid);
        log.setMidPrice(mid);
        log.setSourceSystem("BLOOMBERG");

        forexQuoteLogRepository.save(log);
    }

    /**
     * 更新头寸 (核心风控 + 成本核算)
     *
     * @param currency    币种
     * @param amountDelta 变动金额 (+为银行买入/用户卖出, -为银行卖出/用户买入)
     * @param rateToMop 该币种对 MOP 的汇率 (App层必须传进来，不能瞎算)
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateBankPosition(String currency, BigDecimal amountDelta, BigDecimal rateToMop) {
        BankPosition pos = bankPositionRepository.findByCurrency(currency);

        if (pos == null) {
            throw new BusinessException("E000", "无银行头寸: " + currency);
        }

        // 0. 状态检查 (熔断机制)
        if (PositionStatus.STOPPED == pos.getStatus()) {
            throw new BusinessException("E009", "银行头寸交易熔断暂停: " + currency);
        }

        // 1. 风控检查
        BigDecimal newTotal = pos.getTotalAmount().add(amountDelta);
        if (newTotal.compareTo(pos.getRiskLimitMax()) > 0) {
            throw new BusinessException("E001", "银行头寸超限(Max): " + currency);
        }
        if (newTotal.compareTo(pos.getRiskLimitMin()) < 0) {
            throw new BusinessException("E002", "银行头寸超限(Min): " + currency);
        }

        // 2. 成本加权平均计算 (仅当银行买入/头寸增加时)，暂不支持空头平仓的成本计算
        if (amountDelta.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal oldCost = pos.getTotalAmount().multiply(pos.getAverageCost());
            BigDecimal newCost = amountDelta.multiply(rateToMop);

            // 只有当新总数不为0时才更新成本，否则重置
            if (newTotal.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal totalCost = oldCost.add(newCost);
                BigDecimal newAvg = totalCost.divide(newTotal, 8, RoundingMode.HALF_UP);
                pos.setAverageCost(newAvg);
            } else {
                pos.setAverageCost(BigDecimal.ZERO);
            }
        }

        pos.setTotalAmount(newTotal);

        // 3. 乐观锁更新
        if (!bankPositionRepository.update(pos)) {
            throw new BusinessException("E003", "头寸更新并发冲突，请重试");
        }

        // =========================================================================
        // [TODO] 生产级架构优化点 (High Priority)
        // =========================================================================
        // 1. 问题：当前只有余额变更，缺乏流水记录，存在"有账无据"的审计风险。
        // 2. 方案：应在此处在一个事务内插入 `bank_position_log` 表。
        // 3. 结构：id, currency, amount_before, amount_change, amount_after, txn_id
        // 4. 价值：用于日终对账 (Reconciliation) 及故障溯源。
        // =========================================================================
    }

    private BigDecimal calculateDealRate(ForexDirectionEnum direction, BigDecimal marketRate) {
        // (加点差 0.2%)
        BigDecimal spread = new BigDecimal("0.002"); // 20个点差
        if (direction == ForexDirectionEnum.BUY) {
            return marketRate.multiply(BigDecimal.ONE.add(spread)); // 银行卖贵点
        } else {
            return marketRate.multiply(BigDecimal.ONE.subtract(spread)); // 银行买便宜点
        }
    }


}
