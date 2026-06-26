package com.aiknowledgebase.common;

/**
 * ErrorCode 是项目统一业务错误码枚举。
 * 它用 Java enum 固定错误码和默认文案，在本项目中保证后端异常、Spring Security 错误和前端提示使用同一套语义。
 */
public enum ErrorCode {
    BAD_REQUEST(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "请先登录"),
    AUTH_FAILED(40101, "用户名或密码错误"),
    FORBIDDEN(40300, "没有权限访问该资源"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "数据冲突"),
    INTERNAL_ERROR(50000, "系统内部错误");

    private final int code;
    private final String message;

    /**
     * 枚举构造方法绑定业务码和默认文案。
     * 它是 Java enum 自带的构造能力，在本项目中用于定义稳定的前后端错误契约。
     *
     * @param code 业务错误码，前端可据此做错误分支。
     * @param message 默认错误文案，用于 ApiResponse 失败响应。
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * getCode 返回业务错误码。
     * 它被 ApiResponse.java 和 GlobalExceptionHandler.java 调用，在本项目中输出稳定的错误编号。
     *
     * @return 当前枚举项的业务错误码。
     */
    public int getCode() {
        return code;
    }

    /**
     * getMessage 返回默认错误消息。
     * 它被 ApiResponse.java 和 BusinessException.java 调用，在本项目中输出统一错误提示。
     *
     * @return 当前枚举项的默认错误消息。
     */
    public String getMessage() {
        return message;
    }
}
