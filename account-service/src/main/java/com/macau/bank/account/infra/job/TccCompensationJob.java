package com.macau.bank.account.infra.job;

import com.macau.bank.account.application.command.UnfreezeBalanceCmd;
import com.macau.bank.account.application.service.AccountAppService;
import com.macau.bank.account.domain.entity.AccountFreezeLog;
import com.macau.bank.account.domain.repository.AccountFreezeLogRepository;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TCC 事务补偿任务 (兜底机制)
 * 职责：定期捞取 "超时未提交/未回滚" 的冻结记录，执行自动解冻，防止资源永久悬挂。
 */
@Slf4j
@Component
public class TccCompensationJob {

    @Resource
    private AccountAppService accountAppService;
    
    @Resource
    private AccountFreezeLogRepository freezeLogRepository;

    /**
     * 任务名：tccFreezeCompensationJob
     * 调度频率建议：每分钟 1 次
     */
    @XxlJob("tccFreezeCompensationJob")
    public void tccFreezeCompensationJob() {
        log.info("🚀 [TCC补偿] 开始扫描超时冻结记录...");

        // 1. 定义超时时间 (比如 30分钟前)
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);

        // 2. 捞取数据 (分页捞取，防止一次太多 OOM)
        List<AccountFreezeLog> deadFreezeLogList = freezeLogRepository.findDeadLogs(timeoutThreshold, 100);

        if (deadFreezeLogList.isEmpty()) {
            log.info("✅ [TCC补偿] 无超时记录，任务结束。");
            return;
        }

        log.info("⚠️ [TCC补偿] 发现 {} 条死单，开始执行强制解冻...", deadFreezeLogList.size());

        // 3. 逐条执行 Cancel (复用之前的 Cancel 逻辑)
        for (AccountFreezeLog logEntry : deadFreezeLogList) {
            try {
                // 调用你 Day 4 写的 cancel 方法
                UnfreezeBalanceCmd cmd = UnfreezeBalanceCmd.builder()
                        .accountNo(logEntry.getAccountNo())
                        .amount(logEntry.getAmount())
                        .currencyCode(logEntry.getCurrencyCode())
                        .flowNo(logEntry.getFlowNo())
                        .reason("冻结超时资金解冻")
                        .build();
                accountAppService.unfreezeBalance(cmd);
                log.info("   -> 修复成功: txnId={}", logEntry.getFlowNo());
            } catch (Exception e) {
                log.error("   -> 修复失败: txnId={}", logEntry.getFlowNo(), e);
                // 此时可以发钉钉/企业微信报警，人工介入
            }
        }
    }
}