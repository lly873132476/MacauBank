package com.macau.bank.transfer.interfaces.scheduler; // 修正包名

import com.macau.bank.transfer.application.service.TransferAppService; 
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

/**
 * 接口层：定时任务适配器 (Driving Adapter)
 * 职责：只负责接收调度指令，不写复杂业务逻辑，直接委派给 Application 层
 */
@Component
public class ReconciliationJob {

    // 注入应用层服务 (Application Service)
    @Resource
    private TransferAppService transferAppService;

    @XxlJob("dailyReconciliationHandler")
    public void dailyReconciliationHandler() {
        // 1. 获取参数 (分片等)
        int shardIndex = XxlJobHelper.getShardIndex();
        
        XxlJobHelper.log("开始调度对账任务...");

        // 2. 委派给应用层执行 (Orchestration)
        transferAppService.executeDailyReconciliation(shardIndex);
        
        XxlJobHelper.log("调度结束");
    }
}