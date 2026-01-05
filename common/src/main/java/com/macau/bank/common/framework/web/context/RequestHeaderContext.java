package com.macau.bank.common.framework.web.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.macau.bank.common.framework.web.model.BaseRequest;

public class RequestHeaderContext {
    // 使用 TTL (TransmittableThreadLocal) 可以在线程池中传递上下文
    private static final ThreadLocal<BaseRequest> CONTEXT = new TransmittableThreadLocal<>();

    public static void set(BaseRequest request) {
        CONTEXT.set(request);
    }

    public static BaseRequest get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}