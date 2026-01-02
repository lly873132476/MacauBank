package com.macau.bank.transfer.infra.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.executor.appname}")
    private String appName;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init. adminAddress: {}", adminAddresses);
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appName); // 核心：必须和网页上填的一样
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);

        if (ip != null && !ip.trim().isEmpty()) {
            xxlJobSpringExecutor.setIp(ip);
        }

        xxlJobSpringExecutor.setLogPath(logPath);
        // 自动获取 IP，日志目录等使用默认即可
        return xxlJobSpringExecutor;
    }
}