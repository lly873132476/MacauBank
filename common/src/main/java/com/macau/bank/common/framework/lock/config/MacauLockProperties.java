package com.macau.bank.common.framework.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "macau.lock")
public class MacauLockProperties {
    /** 默认等待时间 (秒) */
    private long waitTime = 3;
    /** 默认持有时间 (秒) */
    private long leaseTime = 10;
}