package com.aiknowledgebase.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * RegisterRequest 是注册接口的请求 DTO。
 * 它使用 Spring Validation 自带的 Email、NotBlank、Size 注解校验前端输入，在本项目中作为创建用户的输入边界。
 *
 * @param username 前端注册表单传入的用户名，后续会由 AuthService 用 UserMapper 查询 users 表做唯一性校验。
 * @param password 前端注册表单传入的明文密码，AuthService 会用 Spring Security BCrypt 加密后再保存。
 * @param nickname 前端注册表单传入的昵称，用于用户资料展示。
 * @param email 前端注册表单传入的邮箱，@Email 是 Spring Validation 自带的邮箱格式校验。
 */
public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        String username,

        @NotBlank
        @Size(min = 6, max = 72)
        String password,

        @NotBlank
        @Size(max = 64)
        String nickname,

        @Email
        @Size(max = 128)
        String email
) {
}
