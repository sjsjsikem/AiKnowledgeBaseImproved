package com.aiknowledgebase.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest 是登录接口的请求 DTO。
 * 它使用 Spring Validation 自带的字段校验注解约束用户名和密码，在本项目中承接前端登录表单。
 *
 * @param username 前端登录表单传入的用户名，@NotBlank 是 Spring Validation 自带的非空校验。
 * @param password 前端登录表单传入的明文密码，只在请求链路中短暂使用，后端会用 BCrypt 哈希校验。
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
