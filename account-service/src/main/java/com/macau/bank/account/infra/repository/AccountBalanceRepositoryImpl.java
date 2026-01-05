package com.macau.bank.account.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macau.bank.account.common.result.AccountErrorCode;
import com.macau.bank.account.domain.entity.AccountBalance;
import com.macau.bank.account.domain.repository.AccountBalanceRepository;
import com.macau.bank.account.infra.converter.AccountBalanceConverter;
import com.macau.bank.account.infra.mapper.AccountBalanceMapper;
import com.macau.bank.account.infra.persistent.po.AccountBalancePO;
import com.macau.bank.account.infra.util.AccountSecurityUtil;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.exception.FatalSystemException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账户余额仓储实现
 * <p>
 * 核心安全特性：HMAC 余额防篡改校验
 * </p>
 */
@Slf4j
@Repository
public class AccountBalanceRepositoryImpl implements AccountBalanceRepository {

    @Resource
    private AccountBalanceMapper accountBalanceMapper;

    @Resource
    private AccountSecurityUtil accountSecurityUtil;

    @Resource
    private AccountBalanceConverter accountBalanceConverter;

    @Override
    public AccountBalance findByAccountAndCurrency(String accountNo, String currencyCode) {
        LambdaQueryWrapper<AccountBalancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountBalancePO::getAccountNo, accountNo);
        wrapper.eq(AccountBalancePO::getCurrencyCode, currencyCode);

        AccountBalancePO po = accountBalanceMapper.selectOne(wrapper);
        if (po == null) {
            return null;
        }

        // 【安全校验】验证 HMAC
        verifyMacCode(po);

        return accountBalanceConverter.toEntity(po);
    }

    @Override
    public List<AccountBalance> findByAccountNo(String accountNo) {
        LambdaQueryWrapper<AccountBalancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountBalancePO::getAccountNo, accountNo);

        return accountBalanceMapper.selectList(wrapper).stream()
                .map(po -> {
                    // 【安全校验】列表查询同样需要逐条验证 HMAC
                    verifyMacCode(po);
                    return accountBalanceConverter.toEntity(po);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountBalance> findByAccountNos(List<String> accountNos) {
        if (accountNos == null || accountNos.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<AccountBalancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AccountBalancePO::getAccountNo, accountNos);

        return accountBalanceMapper.selectList(wrapper).stream()
                .map(po -> {
                    // 【安全校验】批量查询同样需要逐条验证 HMAC
                    verifyMacCode(po);
                    return accountBalanceConverter.toEntity(po);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void save(AccountBalance entity) {
        if (entity == null) {
            return;
        }

        AccountBalancePO po = accountBalanceConverter.toPO(entity);

        if (po.getId() == null) {
            // 【新增】初始化版本号和时间戳
            po.setVersion(0);
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            // 计算初始 MAC (version = 0)
            po.setMacCode(accountSecurityUtil.calculateMac(po.getBalance(), 0));

            accountBalanceMapper.insert(po);
            entity.setId(po.getId());
            entity.setVersion(0);
        } else {
            // 【更新】
            // PO 中的 version 是旧版本号 (如 version=1)
            // 更新后数据库中的 version 将变为 2
            Integer nextVersion = po.getVersion() + 1;

            // 基于新状态计算 MAC
            String mac = accountSecurityUtil.calculateMac(po.getBalance(), nextVersion);
            po.setMacCode(mac);
            po.setUpdateTime(LocalDateTime.now());

            // 执行更新
            // 使用自定义 updateOptimistic 确保乐观锁 (SET version = version + 1 WHERE version =
            // oldVersion)
            // 避免 MyBatis Plus 插件配置失效导致并发安全问题
            log.info("Try updateById: id={}, ver={}, balance={}", po.getId(), po.getVersion(), po.getBalance());
            int rows = accountBalanceMapper.updateOptimistic(po);
            log.info("Updated rows: {}", rows);

            if (rows == 0) {
                // 乐观锁冲突：其他线程已修改该记录
                throw new BusinessException(AccountErrorCode.CONCURRENCY_CONFLICT);
            }

            // 同步 Entity 状态与数据库保持一致
            entity.setVersion(nextVersion);
            entity.setMacCode(mac);
        }
    }

    /**
     * 验证 HMAC 防篡改码
     * <p>
     * 如果校验失败，说明数据库数据被非法篡改，立即触发致命告警并阻断请求
     * </p>
     *
     * @param po 待校验的持久化对象
     */
    private void verifyMacCode(AccountBalancePO po) {
        if (po.getMacCode() != null) {
            boolean valid = accountSecurityUtil.verify(po.getBalance(), po.getVersion(), po.getMacCode());
            if (!valid) {
                log.error("【致命告警】账户余额数据疑似被篡改! AccountNo={}, Currency={}, Balance={}, Version={}, DB_MAC={}",
                        po.getAccountNo(), po.getCurrencyCode(), po.getBalance(), po.getVersion(), po.getMacCode());
                throw new FatalSystemException("数据完整性校验失败：账户余额 HMAC 不匹配");
            }
        }
    }
}
