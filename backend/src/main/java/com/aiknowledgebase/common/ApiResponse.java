package com.aiknowledgebase.common;

/**
 * ApiResponse 是后端所有接口的统一响应模型。
 * 它用 Java record 表达 code、message、data 三段式结构，在本项目中让前端 Axios 客户端可以统一解包和处理错误。
 *
 * @param code 业务错误码，0 表示成功，其他值来自 ErrorCode.java。
 * @param message 响应消息，成功时为 success，失败时来自 ErrorCode.java 或业务异常。
 * @param data 接口业务数据，成功时由 Controller 或 Service 返回。
 */
public record ApiResponse<T>(int code, String message, T data) {

    /**
     * success 使用业务数据创建成功响应。
     * 它通过 Java 泛型保留 data 类型，在本项目中让 Controller 可以返回任意业务 DTO。
     *
     * @param data 来自具体业务方法的返回数据，例如 AuthResponse 或 UserProfile。
     * @return ApiResponse 包装后的成功响应。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    /**
     * success 创建没有业务数据的成功响应。
     * 它用于 logout 等只需要表达操作成功的接口，在本项目中保持前后端响应格式一致。
     *
     * @return ApiResponse 空成功响应。
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(0, "success", null);
    }

    /**
     * failure 使用 ErrorCode 创建失败响应。
     * 它把 ErrorCode.java 中的标准错误码转换为统一响应体，在本项目中服务全局异常和安全异常输出。
     *
     * @param errorCode 来自 ErrorCode.java 的业务错误枚举。
     * @return ApiResponse 空数据失败响应。
     */
    public static ApiResponse<Void> failure(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * failure 使用显式 code 和 message 创建失败响应。
     * 它允许 BusinessException 覆盖默认错误文案，在本项目中支持更具体的业务提示。
     *
     * @param code 业务错误码，通常来自 ErrorCode.java。
     * @param message 失败消息，通常来自异常或校验结果。
     * @return ApiResponse 空数据失败响应。
     */
    public static ApiResponse<Void> failure(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
