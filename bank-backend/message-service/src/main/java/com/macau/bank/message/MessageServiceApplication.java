package com.macau.bank.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.macau.bank.message.infrastructure.mapper")
@ComponentScan(basePackages = {"com.macau.bank.message", "com.macau.bank.common"})
public class MessageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageServiceApplication.class, args);
    }
}
