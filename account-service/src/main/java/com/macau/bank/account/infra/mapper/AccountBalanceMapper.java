package com.macau.bank.account.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macau.bank.account.infra.persistent.po.AccountBalancePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface AccountBalanceMapper extends BaseMapper<AccountBalancePO> {

    /**
     * 自定义 SQL 更新余额 (带乐观锁) - 现需同时更新 macCode
     */
    int updateBalance(@Param("accountNo") String accountNo,
            @Param("currencyCode") String currencyCode,
            @Param("amount") BigDecimal amount,
            @Param("version") Integer version);

    /**
     * 冻结
     */
    int freezeBalance(@Param("accountNo") String accountNo,
            @Param("currencyCode") String currencyCode,
            @Param("amount") BigDecimal amount,
            @Param("version") Integer version);

    /**
     * 解冻
     */
    int unfreezeBalance(@Param("accountNo") String accountNo,
            @Param("currencyCode") String currencyCode,
            @Param("amount") BigDecimal amount,
            @Param("version") Integer version);

    /**
     * 全字段乐观锁更新 (绕过 MP 插件，手动控制)
     */
    int updateOptimistic(AccountBalancePO po);

    AccountBalancePO findUserBalance(@Param("userNo") String userNo, @Param("currencyCode") String currencyCode);
}