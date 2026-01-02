package com.macau.bank.common.core.constant;

/**
 * MQ Topic 常量池
 * <p>
 * 命名规范：TP_业务域_描述
 * 所有的 Topic 必须在此统一注册，禁止在代码里硬编码字符串。
 */
public class MqTopicConst {

    /**
     * [风控域] 风控请求 Topic
     * 生产者: Transfer Service
     * 消费者: Risk Service
     */
    public static final String TP_RISK_CHECK_REQUEST = "TP_RISK_CHECK_REQUEST";

    /**
     * [风控域] 风控结果回调 Topic
     * 生产者: Risk Service / Mock Controller
     * 消费者: Transfer Service (RiskCallbackConsumer)
     */
    public static final String TP_RISK_CALLBACK = "TP_RISK_CALLBACK";

    /**
     * [用户域] 用户审核通过 (开户)
     * 生产者: User Service / Admin
     * 消费者: Account Service
     */
    public static final String TP_USER_AUDIT_PASS = "TP_USER_AUDIT_PASS";

    /**
     * [用户域] 用户安全等级变更
     * 生产者: User Service / Admin
     * 消费者: Message Service (UserLevelMessageConsumer)
     */
    public static final String TP_USER_SECURITY = "TP_USER_SECURITY";

    /**
     * [通知域] 短信通知
     * 生产者: Any Service
     * 消费者: Sms Service
     */
    public static final String TP_SMS_NOTIFY = "TP_SMS_NOTIFY";
    
    // 私有构造，防止实例化
    private MqTopicConst() {}
}