package com.macau.bank.gateway;

import com.macau.bank.common.framework.web.config.CommonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 网关服务启动类
 * 基于 Spring Cloud Gateway 的API网关
 * 提供统一路由、CORS配置等功能
 */
// 重点看这里：exclude 排除掉 Common 里的 Web 自动配置
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class, // Gateway 通常不连数据库，所以也要排除数据源
        CommonAutoConfiguration.class   // 🚨【关键修改】Gateway 不使用 Servlet 的那一套配置
})
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
