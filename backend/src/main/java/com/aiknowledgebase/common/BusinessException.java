package com.aiknowledgebase.common;

/**
 * BusinessException 是项目自定义业务异常。
 * 它继承 Java 自带 RuntimeException，并携带 ErrorCode.java，在本项目中让 Service 可以用异常中断失败业务流程。
 */
public class BusinessException extends RuntimeException {

    // errorCode 来自 ErrorCode.java，用于全局异常处理器生成统一错误响应。
    private final ErrorCode errorCode;

    /**
     * 构造方法使用 ErrorCode 的默认消息创建业务异常。
     * 它通过 RuntimeException 保存错误文案，在本项目中适合通用错误场景。
     *
     * @param errorCode 来自 ErrorCode.java 的标准业务错误码。
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造方法使用 ErrorCode 和自定义消息创建业务异常。
     * 它通过覆盖默认文案提供更具体提示，在本项目中适合“用户名已存在”等场景。
     *
     * @param errorCode 来自 ErrorCode.java 的标准业务错误码。
     * @param message 当前业务场景的自定义错误消息。
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * getErrorCode 返回业务异常携带的错误码。
     * 它被 GlobalExceptionHandler.java 调用，在本项目中把异常转换为 ApiResponse。
     *
     * @return ErrorCode 当前异常对应的业务错误码。
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
