package com.macau.bank.transfer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 转账服务启动类
 * <p>
 * 职责：
 * - 提供转账业务核心功能（行内转账、跨境转账、转数快等）
 * - 管理收款人信息
 * - 风控流程编排
 * <p>
 * 技术栈：
 * - Dubbo RPC 服务提供者
 * - RocketMQ 消息消费者（风控回调）
 * - MyBatis-Plus 数据持久化
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("com.macau.bank.transfer.infra.mapper")
@ComponentScan(basePackages = { "com.macau.bank.transfer", "com.macau.bank.common" })
public class TransferServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }
}