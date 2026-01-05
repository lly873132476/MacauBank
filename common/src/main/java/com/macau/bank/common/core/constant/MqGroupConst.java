package com.macau.bank.common.core.constant;

/**
 * MQ ConsumerGroup 常量池
 * <p>
 * 命名规范：GP_服务名_业务描述
 * 警告：不同的微服务禁止使用相同的 Group，否则会导致消息丢失或负载均衡异常。
 */
public class MqGroupConst {

    /**
     * [转账服务] 监听风控回调
     * 对应 Listener: RiskCallbackConsumer
     */
    public static final String GP_TRANSFER_RISK_CALLBACK = "GP_TRANSFER_RISK_CALLBACK";

    /**
     * [账户服务] 监听用户开户审核
     * 对应 Listener: UserAuditMqListener
     */
    public static final String GP_ACCOUNT_USER_AUDIT = "GP_ACCOUNT_USER_AUDIT";

    /**
     * [短信服务] 监听短信发送请求
     */
    public static final String GP_SMS_SENDER = "GP_SMS_SENDER";

    /**
     * [消息服务] 监听用户安全与等级变动
     */
    public static final String GP_MESSAGE_SERVICE_USER = "GP_MESSAGE_SERVICE_USER";

    private MqGroupConst() {}
}