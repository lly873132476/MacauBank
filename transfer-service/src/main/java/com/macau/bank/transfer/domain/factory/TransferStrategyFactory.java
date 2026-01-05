package com.macau.bank.transfer.domain.factory;

import com.macau.bank.common.core.enums.TransferType;
import com.macau.bank.transfer.domain.strategy.TransferStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 转账策略工厂
 * 根据转账类型创建对应的策略实例
 */
@Component
public class TransferStrategyFactory {

    @Resource
    private List<TransferStrategy> strategyList;

    private Map<TransferType, TransferStrategy> strategyMap;

    @PostConstruct
    public void init() {
        this.strategyMap = strategyList.stream().collect(Collectors.toMap(
                TransferStrategy::getTransferType,       // Key 是枚举
                Function.identity(),                     // Value 是策略对象本身
                (oldValue, newValue) -> {                // 🛡️ 防御性编程：如果有重复 Key，直接报错！
                    throw new IllegalStateException("策略冲突！发现多个实现类绑定了同一个转账类型: " + oldValue.getTransferType());
                }
        ));
    }

    /**
     * 根据转账类型获取对应的策略实例
     *
     * @param transferType 转账类型
     * @return 转账策略实例
     */
    public TransferStrategy getStrategy(TransferType transferType) {
        return strategyMap.get(transferType);
    }
}
