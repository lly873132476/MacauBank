package com.macau.bank.common.framework.web.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.exception.FatalSystemException;
import com.macau.bank.common.core.result.Result;
import com.macau.bank.common.core.result.ResultCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有服务的异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Resource
    private MessageSource messageSource;

    // 2. 【核心】处理致命异常
    @ExceptionHandler(FatalSystemException.class)
    public Result<?> handleFatalException(FatalSystemException e) {
        // A. 记录带堆栈的 ERROR 日志
        log.error("【严重安全警报】系统发生致命错误！原因: {}", e.getMessage(), e);

        // B. 异步发送报警 (这就叫“吹哨人”)
        // 发送到你们的项目群： "Account服务出现资损风险！请求参数与数据库不一致！请立刻排查！"

        // C. 返回给前端一个模糊的错误，不要把具体的 "金额不一致" 告诉黑客
        return Result.fail(500, "系统内部错误，安全风控已拦截，请联系客服");
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        String message = e.getMessage();
        
        // 尝试获取国际化消息
        if (StringUtils.hasText(e.getI18nKey())) {
            try {
                message = messageSource.getMessage(e.getI18nKey(), e.getArgs(), e.getMessage(), LocaleContextHolder.getLocale());
            } catch (Exception ex) {
                log.warn("获取国际化消息失败: key={}, locale={}", e.getI18nKey(), LocaleContextHolder.getLocale());
            }
        }
        
        log.warn("业务异常: uri={}, code={}, message={}, i18nKey={}", request.getRequestURI(), e.getCode(), message, e.getI18nKey());
        return Result.fail(e.getCode(), message);
    }


    @ExceptionHandler(RpcException.class)
    public Result<?> handleRpcException(RpcException e, HttpServletRequest request) {
        // 记录更详细的上下文
        log.error("RPC 调用异常: uri={}, method={}, error={}", request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        if (e.isTimeout()) {
            return Result.fail(504, "服务响应超时，请稍后重试");
        }
        return Result.fail(503, "依赖服务暂时不可用");
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = this.handleBindingResult(e.getBindingResult());
        log.warn("参数校验异常: uri={}, message={}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_ERROR, message);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        // 拼接错误信息，例如 "Token不能为空"
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.PARAM_ERROR, message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = this.handleBindingResult(e.getBindingResult());
        log.warn("参数绑定异常: uri={}, message={}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_ERROR, message);
    }

    /**
     * 处理404异常
     * 需要在application.yml配置:
     * spring.mvc.throw-exception-if-no-handler-found: true
     * spring.web.resources.add-mappings: false
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("路径不存在: uri={}", request.getRequestURI());
        return Result.fail(404, "路径不存在");
    }

    /**
     * 兜底处理 RuntimeException
     * 核心作用：识别被 Seata 或其他 AOP 框架包装过的业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        // 1. 尝试“拆包”：看看异常的起因（Cause）是不是 BusinessException
        Throwable cause = e.getCause();

        // 循环拆包（防止有时候包了两层）
        while (cause != null) {
            if (cause instanceof BusinessException) {
                // 💡 关键点：如果发现核心是业务异常，直接转交给上面的方法处理！
                // 这样日志和返回格式就完美复用了，不用写两遍代码
                return handleBusinessException((BusinessException) cause, request);
            }
            cause = cause.getCause();
        }

        // 2. 如果拆到底都不是 BusinessException，那才是真正的系统 Bug
        log.error("系统异常: uri={}", request.getRequestURI(), e); // 这里才需要打印堆栈
        return Result.fail(ResultCode.FAIL.getCode(), "系统繁忙，请稍后再试");
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: uri={}", request.getRequestURI(), e);
        return Result.fail(ResultCode.FAIL);
    }

    // 这样整个项目所有接口限流了，都会走这个逻辑，不用每个都写
    @ExceptionHandler(BlockException.class)
    public Result<?> handleBlockException(BlockException e) {
        log.warn("触发限流: {}", e.getRule().getResource());
        return Result.fail(429, "系统繁忙，请稍后重试"); // 对应 HTTP 429
    }

    /**
     * 处理BindingResult
     */
    private String handleBindingResult(BindingResult result) {
        if (result.hasErrors()) {
            FieldError error = result.getFieldError();
            if (error != null) {
                return error.getDefaultMessage();
            }
        }
        return "参数错误";
    }

}
