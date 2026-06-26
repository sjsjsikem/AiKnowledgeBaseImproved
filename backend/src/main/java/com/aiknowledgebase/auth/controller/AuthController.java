package com.aiknowledgebase.auth.controller;

import com.aiknowledgebase.auth.dto.AuthResponse;
import com.aiknowledgebase.auth.dto.LoginRequest;
import com.aiknowledgebase.auth.dto.RegisterRequest;
import com.aiknowledgebase.auth.dto.UserProfile;
import com.aiknowledgebase.auth.service.AuthService;
import com.aiknowledgebase.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthController 是认证模块的 HTTP 入口。
 * 它使用 Spring MVC 的 Controller 注解接收前端登录、注册和退出请求，并把请求交给 AuthService 完成业务处理。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    // authService 来自 AuthService.java，负责注册、登录、当前用户等认证业务。
    private final AuthService authService;

    /**
     * 构造方法由 Spring 注入认证业务服务。
     * 它让 Controller 保持轻薄，只负责 HTTP 适配，不直接操作数据库或 JWT。
     *
     * @param authService 来自 AuthService.java，承接认证业务逻辑。
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * register 是注册接口。
     * 它接收前端 RegisterRequest，调用 AuthService 创建用户并返回统一 ApiResponse。
     *
     * @param request 来自 RegisterRequest.java，由 Spring Validation 校验字段合法性。
     * @return ApiResponse 包装 AuthResponse，供前端保存 Token 和用户资料。
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    /**
     * login 是登录接口。
     * 它接收前端 LoginRequest，调用 AuthService 校验密码并签发 JWT。
     *
     * @param request 来自 LoginRequest.java，由前端登录表单提交。
     * @return ApiResponse 包装 AuthResponse，供前端进入受保护路由。
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    /**
     * me 是认证模块下的当前用户接口。
     * 它通过 AuthService 读取 SecurityContext，在本项目中用于验证 Bearer Token 是否有效。
     *
     * @return ApiResponse 包装 UserProfile，返回当前登录用户资料。
     */
    @GetMapping("/me")
    public ApiResponse<UserProfile> me() {
        return ApiResponse.success(authService.currentUser());
    }

    /**
     * logout 是退出登录接口。
     * Stage 1 只返回成功，由前端清除 Token；Stage 5 接入 Redis 后会扩展为服务端 Token 黑名单。
     *
     * @return ApiResponse 空成功响应，保持前后端统一交互格式。
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }
}
