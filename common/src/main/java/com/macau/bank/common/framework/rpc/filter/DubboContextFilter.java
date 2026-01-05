package com.macau.bank.common.framework.rpc.filter;

import com.macau.bank.common.core.constant.CommonConstant;
import com.macau.bank.common.framework.web.context.RequestHeaderContext;
import com.macau.bank.common.framework.web.model.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.MDC;

/**
 * Dubbo 上下文传递过滤器
 * 作用：在微服务之间自动透传 traceId, userNo 等隐式参数
 */
// 激活条件：同时在 Consumer 和 Provider 端生效
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
@Slf4j
public class DubboContextFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 判断当前是 Consumer 还是 Provider
        boolean isConsumer = RpcContext.getContext().isConsumerSide();

        if (isConsumer) {
            // ========= Consumer 端：把 ThreadLocal 里的数据 塞进 Dubbo 上下文 =========
            BaseRequest context = RequestHeaderContext.get();
            if (context != null) {
                // setAttachment 会把数据放到网络请求的 header 里传过去
                RpcContext.getContext().setAttachment(CommonConstant.LOG_TRACE_ID, context.getTraceId());
                RpcContext.getContext().setAttachment(CommonConstant.LOG_USER_NO, context.getUserNo());
            }
        } else {
            // ========= Provider 端：从 Dubbo 上下文 读取数据 塞进 ThreadLocal =========
            String traceId = RpcContext.getContext().getAttachment(CommonConstant.LOG_TRACE_ID);
            String userNo = RpcContext.getContext().getAttachment(CommonConstant.LOG_USER_NO);

            // 1. 重建 Context
            BaseRequest context = new BaseRequest();
            context.setTraceId(traceId);
            context.setUserNo(userNo);
            RequestHeaderContext.set(context);

            // 2. 放入 MDC (为了让 Provider 的日志也能打印 traceId)
            if (traceId != null) MDC.put(CommonConstant.LOG_TRACE_ID, traceId);
            if (userNo != null) MDC.put(CommonConstant.LOG_USER_NO, userNo);
        }

        try {
            // 执行真正的业务逻辑
            return invoker.invoke(invocation);
        } finally {
            //如果是 Provider，执行完必须清理 ThreadLocal，防止内存泄漏
            if (!isConsumer) {
                RequestHeaderContext.clear();
                MDC.clear();
            }
        }
    }
}