package com.macau.bank.account.infra.tcc;

import com.macau.bank.account.domain.entity.AccountFreezeLog;
import com.macau.bank.account.domain.repository.AccountFreezeLogRepository;
import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.FreezeStatus;
import com.macau.bank.common.core.enums.FreezeType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * TCC 协议保护服务
 * <p>
 * 职责：封装分布式事务协议相关的技术细节
 * - 悬挂检测（防止 Cancel 先于 Try 执行导致资源悬挂）
 * - 空回滚检测（防止 Try 未执行时直接执行 Cancel）
 * <p>
 * 设计说明：
 * 此服务属于 Infrastructure 层，将 TCC 协议细节与 Domain 层隔离，
 * 使 Domain 层保持纯业务逻辑，不感知分布式事务框架。
 */
@Slf4j
@Service
public class TccProtectionService {

    @Resource
    private AccountFreezeLogRepository accountFreezeLogRepository;

    /**
     * Try 阶段防悬挂检查
     * <p>
     * 场景：Cancel 先于 Try 到达（网络延迟导致），此时应拒绝 Try 执行
     *
     * @param flowNo 业务流水号
     * @return true=允许执行 Try, false=悬挂拦截（Cancel 已执行）
     */
    public boolean checkTryAllowed(String flowNo) {
        AccountFreezeLog existingLog = accountFreezeLogRepository.findByFlowNo(flowNo);
        if (existingLog != null && existingLog.getStatus() == FreezeStatus.CANCELLED) {
            log.warn("[TCC防悬挂] Cancel 已执行，拒绝 Try: flowNo={}", flowNo);
            return false;
        }
        return true;
    }

    /**
     * Cancel 阶段空回滚处理
     * <p>
     * 场景：Try 未执行（超时/异常）时，Cancel 先到达，需插入标记防止后续 Try 执行
     *
     * @param flowNo    业务流水号
     * @param accountNo 账户号
     * @param amount    金额
     * @return true=正常 Cancel（Try 已执行）, false=空回滚（已插入标记）
     */
    public boolean handleEmptyRollback(String flowNo, String accountNo, Money amount) {
        AccountFreezeLog freezeLog = accountFreezeLogRepository.findByFlowNo(flowNo);

        if (freezeLog == null) {
            // Try 未执行，插入 Cancel 标记
            AccountFreezeLog cancelMarker = new AccountFreezeLog();
            cancelMarker.setFlowNo(flowNo);
            cancelMarker.setAccountNo(accountNo);
            cancelMarker.setCurrencyCode(amount.getCurrencyCode());
            cancelMarker.setAmount(amount.getAmount());
            cancelMarker.setFreezeType(FreezeType.TRANSACTION);
            cancelMarker.setStatus(FreezeStatus.CANCELLED);
            cancelMarker.setReason("TCC空回滚标记");
            cancelMarker.setCreateTime(LocalDateTime.now());
            accountFreezeLogRepository.save(cancelMarker);

            log.warn("[TCC空回滚] Try 未执行，已插入 Cancel 标记: flowNo={}, accountNo={}", flowNo, accountNo);
            return false;
        }

        // 检查是否已处理过
        if (freezeLog.getStatus() == FreezeStatus.CANCELLED) {
            log.warn("[TCC幂等] 空回滚标记已存在，跳过: flowNo={}", flowNo);
            return false;
        }

        if (freezeLog.getStatus() == FreezeStatus.UNFROZEN) {
            log.warn("[TCC幂等] 已解冻，跳过: flowNo={}", flowNo);
            return false;
        }

        if (freezeLog.getStatus() == FreezeStatus.DEDUCTED) {
            log.warn("[TCC幂等] 已扣款，跳过解冻: flowNo={}", flowNo);
            return false;
        }

        return true;
    }
}
