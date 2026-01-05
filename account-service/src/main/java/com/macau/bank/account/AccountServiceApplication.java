package com.macau.bank.account;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;

/**
 * 账户服务启动类
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("com.macau.bank.account.infra.mapper")
@ComponentScan(basePackages = {"com.macau.bank.account", "com.macau.bank.common"})
@EnableRetry
public class AccountServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
