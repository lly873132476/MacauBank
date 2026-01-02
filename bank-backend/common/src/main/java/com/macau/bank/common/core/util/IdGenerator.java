package com.macau.bank.common.core.util;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * 分布式ID生成器 (基于 Twitter Snowflake 算法)
 * 结构：0(1位) - 时间戳(41位) - 数据中心ID(5位) - 机器ID(5位) - 序列号(12位)
 */
public class IdGenerator {

    // 起始时间戳 (例如：2024-01-01 00:00:00)
    private final static long START_TIMESTAMP = 1704067200000L;

    // 各部分占用的位数
    private final static long SEQUENCE_BIT = 12; // 序列号占用的位数
    private final static long MACHINE_BIT = 5;   // 机器标识占用的位数
    private final static long DATACENTER_BIT = 5;// 数据中心占用的位数

    // 各部分最大值
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    // 每一部分向左的位移
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId;  // 数据中心ID
    private long machineId;     // 机器ID
    private long sequence = 0L; // 序列号
    private long lastTimestamp = -1L; // 上次生成ID的时间戳

    // 单例模式 (或者作为 Spring Bean 注入)
    private static IdGenerator INSTANCE = new IdGenerator();

    private IdGenerator() {
        this.datacenterId = getDatacenterId();
        this.machineId = getMachineId();
    }
    
    public static IdGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * 核心方法：获取下一个 ID
     */
    public synchronized long nextId() {
        long currTimestamp = timeGen();

        // 时钟回拨检查
        if (currTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        // 如果是同一毫秒内生成的
        if (currTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 毫秒内序列溢出
            if (sequence == 0L) {
                // 阻塞到下一毫秒,获得新的时间戳
                currTimestamp = getNextMill();
            }
        } else {
            // 如果是新的一毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = currTimestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((currTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT) // 时间戳
                | (datacenterId << DATACENTER_LEFT)                  // 数据中心
                | (machineId << MACHINE_LEFT)                        // 机器ID
                | sequence;                                          // 序列号
    }
    
    /**
     * 生成带业务前缀的字符串ID (推荐使用)
     * 例如: TR + 123456789...
     */
    public static String generateId(String prefix) {
        return prefix + INSTANCE.nextId();
    }
    
    /**
     * 生成无前缀字符串ID
     */
    public static String generateId() {
        return String.valueOf(INSTANCE.nextId());
    }

    private long getNextMill() {
        long mill = timeGen();
        while (mill <= lastTimestamp) {
            mill = timeGen();
        }
        return mill;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    // 自动根据IP获取机器ID (简化版，生产环境建议配合Redis或Zookeeper分配)
    private long getMachineId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            long id;
            if (network == null) {
                id = 1;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    id = ((0x000000FF & (long) mac[mac.length - 1]) | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                    id = id % (MAX_MACHINE_NUM + 1);
                } else {
                    id = 1;
                }
            }
            return id;
        } catch (Exception e) {
            return 1; // 兜底
        }
    }

    // 自动根据PID或HostName获取数据中心ID
    private long getDatacenterId() {
        try {
            long id = 0L;
            String hostName = InetAddress.getLocalHost().getHostName();
            id = hostName.hashCode();
            id = id & MAX_DATACENTER_NUM;
            return id;
        } catch (Exception e) {
            return 1; // 兜底
        }
    }
}