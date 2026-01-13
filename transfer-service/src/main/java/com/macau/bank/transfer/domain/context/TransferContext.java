package com.macau.bank.transfer.domain.context;

import com.macau.bank.common.core.enums.UserLevel;
import com.macau.bank.transfer.domain.entity.TransferOrder;
import com.macau.bank.transfer.domain.model.AccountSnapshot;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 转账全链路上下文对象
 * <p>
 * 作用：在策略模式的各个生命周期节点之间传递数据，避免方法参数过长
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferContext {

    // 1. 核心数据源 (唯一真理)
    // 所有跟“单子”有关的字段，都找它要
    private TransferOrder order;

    // 2. 过程辅助数据 (Order里没有的)
    private AccountSnapshot payerAccount;
    private AccountSnapshot payeeAccount;
    private UserLevel payerUserLevel;
    private String transactionPassword; // 敏感数据只在内存传

    // ===========================
    // 3. 扩展槽 (Extension)
    // ===========================
    /**
     * 扩展属性
     */
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 快捷设置扩展属性
     */
    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    /**
     * 快捷获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) this.attributes.get(key);
    }

    /**
     * 方便获取金额的快捷方法 (可选，为了代码写着顺手)
     * 但底层还是代理给 Order
     */
    public BigDecimal getAmount() {
        return (order != null && order.getAmount() != null) ? order.getAmount().getAmount() : BigDecimal.ZERO;
    }
}