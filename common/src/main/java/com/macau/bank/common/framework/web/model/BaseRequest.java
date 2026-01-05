package com.macau.bank.common.framework.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseRequest implements Serializable {
    /** 链路追踪ID */
    private String traceId;
    /** 当前操作的用户No (解析Token后注入) */
    @JsonIgnore
    private String userNo;
    /** 设备ID (风控用) */
    private String deviceId;
    /** 客户端版本 */
    private String appVersion;
    /** 客户端真实IP */
    private String clientIp;
}