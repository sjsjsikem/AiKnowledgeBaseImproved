package com.aiknowledgebase.auth.controller;

import com.aiknowledgebase.auth.dto.UserProfile;
import com.aiknowledgebase.auth.service.AuthService;
import com.aiknowledgebase.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserController 提供用户资源相关 HTTP 接口。
 * 它使用 Spring MVC Controller 注解暴露 /users/me，在本项目中让前端刷新页面后用 Token 恢复当前用户资料。
 */
@RestController
@RequestMapping("/users")
public class UserController {

    // authService 来自 AuthService.java，当前阶段复用认证服务读取当前用户资料。
    private final AuthService authService;

    /**
     * 构造方法由 Spring 注入 AuthService。
     * 它让用户接口复用认证模块中的当前用户查询能力。
     *
     * @param authService 来自 AuthService.java，用于读取当前登录用户。
     */
    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * me 是 REST 风格的当前用户接口。
     * 它通过 AuthService 从 Spring Security 上下文获取当前用户，在前端登录态恢复中使用。
     *
     * @return ApiResponse 包装 UserProfile，返回当前登录用户资料。
     */
    @GetMapping("/me")
    public ApiResponse<UserProfile> me() {
        return ApiResponse.success(authService.currentUser());
    }
}
