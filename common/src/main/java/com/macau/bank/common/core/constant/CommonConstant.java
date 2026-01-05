package com.macau.bank.common.core.constant;

/**
 * 全局公共常量
 * 统一管理 HTTP Header、MDC Key、Redis Key 前缀等
 */
public class CommonConstant {

    /** 私有构造，防止实例化 */
    private CommonConstant() {}

    /* ============================ HTTP Headers ============================ */
    
    /** 链路追踪 ID (Http Header) */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** 用户 NO (Http Header - 网关透传给微服务用) */
    public static final String USER_NO_HEADER = "X-User-No";
    
    /** 认证 Token */
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    /** 真实 IP */
    public static final String REAL_IP_HEADER = "X-Real-IP";

    /* ============================ Context / MDC Keys ============================ */

    /** 日志链路 ID (Logback MDC Key) */
    public static final String LOG_TRACE_ID = "traceId";

    /** 日志用户 ID (Logback MDC Key) */
    public static final String LOG_USER_NO = "userNo";

    /* ============================ System Defaults ============================ */
    
    /** 默认时区 */
    public static final String DEFAULT_TIMEZONE = "GMT+8";
    
    /** 默认语言 */
    public static final String DEFAULT_LOCALE = "zh_CN";

    /* ============================ Redis Keys ============================ */
    
    /** Auth 服务 Token 前缀: auth:token:uid */
    public static final String REDIS_TOKEN_PREFIX = "auth:token:";
    
    /** Gateway 限流前缀 */
    public static final String REDIS_LIMIT_PREFIX = "gateway:limit:";

    /** 设备信息 */
    public static final String DEVICE_ID_HEADER = "X-Device-Id";
    public static final String APP_VERSION_HEADER = "X-App-Version";
}