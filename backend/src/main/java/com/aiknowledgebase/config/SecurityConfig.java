package com.aiknowledgebase.config;

import com.aiknowledgebase.common.ApiResponse;
import com.aiknowledgebase.common.ErrorCode;
import com.aiknowledgebase.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * SecurityConfig 是后端安全配置类。
 * 它使用 Spring Security 自带的 SecurityFilterChain、PasswordEncoder 等组件，在本项目中定义 JWT 无状态认证和统一安全错误响应。
 */
@Configuration
public class SecurityConfig {

    /**
     * securityFilterChain 配置 Spring Security 的核心过滤器链。
     * 它组合 HttpSecurity、ObjectMapper 和 JwtAuthenticationFilter，在本项目中开放登录注册接口并保护其他 API。
     *
     * @param http Spring Security 自带的 HttpSecurity，用于声明安全规则。
     * @param objectMapper Jackson 提供的 JSON 序列化对象，用于写出统一 ApiResponse 错误响应。
     * @param jwtAuthenticationFilter 来自 JwtAuthenticationFilter.java，用于把 Bearer Token 转换为当前登录用户。
     * @return SecurityFilterChain 是 Spring Security 自带的过滤器链对象。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        http
                // JWT 是无状态认证，不依赖浏览器 Cookie 会话；关闭 CSRF 可以让前后端分离接口更直接。
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/system/info",
                                "/auth/register",
                                "/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin/users/**").hasAuthority("admin:user:read")
                        .requestMatchers(HttpMethod.PATCH, "/admin/users/**").hasAuthority("admin:user:write")
                        .requestMatchers(HttpMethod.PUT, "/admin/users/**").hasAuthority("admin:user:write")
                        .requestMatchers(HttpMethod.GET, "/admin/roles/**", "/admin/permissions/**").hasAuthority("admin:role:read")
                        .requestMatchers(HttpMethod.POST, "/admin/roles/**").hasAuthority("admin:role:write")
                        .requestMatchers(HttpMethod.PUT, "/admin/roles/**").hasAuthority("admin:role:write")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        // 教学重点：Spring Security 的 401/403 默认响应不是项目统一结构，这里手动写成 ApiResponse。
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, objectMapper, HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN)))
                // JWT 过滤器必须放在用户名密码过滤器之前，先把 Bearer Token 转成 SecurityContext。
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * passwordEncoder 创建密码加密器 Bean。
     * 它使用 Spring Security 自带的 BCryptPasswordEncoder，在本项目中为注册和登录提供安全密码哈希能力。
     *
     * @return PasswordEncoder 是 Spring Security 自带接口，具体实现为 BCryptPasswordEncoder。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * writeError 写出 Spring Security 异常的统一 JSON 响应。
     * 它使用 HttpServletResponse 和 Jackson ObjectMapper 手动输出 ApiResponse，在本项目中让 401/403 也遵守统一响应契约。
     *
     * @param response Java Servlet 自带响应对象，用于设置状态码和响应体。
     * @param objectMapper Jackson 提供的 JSON 序列化对象。
     * @param status HTTP 状态码，来自 HttpServletResponse 常量。
     * @param errorCode 来自 ErrorCode.java 的业务错误码。
     */
    private void writeError(HttpServletResponse response, ObjectMapper objectMapper, int status, ErrorCode errorCode)
            throws java.io.IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(errorCode));
    }
}
