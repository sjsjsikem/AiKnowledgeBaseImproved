package com.aiknowledgebase.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler 是后端全局异常处理器。
 * 它使用 Spring MVC 自带的 @RestControllerAdvice 捕获 Controller 链路异常，在本项目中统一输出 ApiResponse 错误结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // log 是 SLF4J 提供的日志对象，用于记录未预期异常和请求定位信息。
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * handleBusinessException 处理业务层主动抛出的 BusinessException。
     * 它根据 ErrorCode 映射 HTTP 状态码并返回 ApiResponse，在本项目中把 Service 的业务失败转换为前端可识别响应。
     *
     * @param ex 来自 BusinessException.java，携带业务错误码和错误消息。
     * @return ResponseEntity 是 Spring 自带响应包装对象，包含 HTTP 状态码和 ApiResponse 错误体。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        // 教学重点：业务错误码和 HTTP 状态码是两层语义。HTTP 状态给客户端/网关判断类别，code 给前端展示和业务分支使用。
        HttpStatus status = switch (errorCode) {
            case UNAUTHORIZED, AUTH_FAILED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.failure(errorCode.getCode(), ex.getMessage()));
    }

    /**
     * handleValidationException 处理 Spring Validation 参数校验异常。
     * 它把 DTO 字段校验失败统一转为 BAD_REQUEST，在本项目中避免每个 Controller 手写参数错误处理。
     *
     * @param ex 来自 Spring Validation 或 Spring MVC 的参数校验异常。
     * @return ResponseEntity 包装后的 400 ApiResponse 错误响应。
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception ex) {
        // 参数校验异常统一在这里收口，Controller 不需要为每个表单字段写重复的 if/else。
        return ResponseEntity.badRequest().body(ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage()));
    }

    /**
     * handleUnknownException 处理未被前面规则捕获的系统异常。
     * 它使用 HttpServletRequest 记录请求路径并隐藏内部细节，在本项目中提供最后一道稳定错误出口。
     *
     * @param ex Java 或框架抛出的未知异常。
     * @param request Spring MVC 注入的 HttpServletRequest，用于读取当前请求路径。
     * @return ResponseEntity 包装后的 500 ApiResponse 错误响应。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownException(Exception ex, HttpServletRequest request) {
        // 未预期异常只在服务端打印详细堆栈，返回给前端的是稳定的通用错误，避免泄漏内部实现细节。
        log.error("Unhandled exception path={}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(ErrorCode.INTERNAL_ERROR));
    }
}
