package com.macau.bank.forex;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 汇率服务启动类
 */
@SpringBootApplication
@EnableDubbo
@ComponentScan(basePackages = {"com.macau.bank.forex", "com.macau.bank.common"})
public class ForexServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForexServiceApplication.class, args);
    }
}
