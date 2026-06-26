package com.aiknowledgebase.security;

import com.aiknowledgebase.auth.service.UserAccessService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthenticationFilter 是 JWT 登录态解析过滤器。
 * 它继承 Spring Web 自带的 OncePerRequestFilter，在本项目中把前端 Authorization Bearer Token 转换成 Spring Security 当前用户。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // jwtService 来自 JwtService.java，用于解析和校验 JWT。
    private final JwtService jwtService;
    // userAccessService 来自 UserAccessService.java，用于根据 JWT 用户 ID 加载数据库中的当前用户。
    private final UserAccessService userAccessService;

    /**
     * 构造方法由 Spring 注入 JWT 和用户访问服务。
     * 它把 Token 解析能力和用户状态加载能力组合起来，在本项目中支撑请求级认证。
     *
     * @param jwtService 来自 JwtService.java，用于解析 Bearer Token。
     * @param userAccessService 来自 UserAccessService.java，用于加载 CurrentUser。
     */
    public JwtAuthenticationFilter(JwtService jwtService, UserAccessService userAccessService) {
        this.jwtService = jwtService;
        this.userAccessService = userAccessService;
    }

    /**
     * doFilterInternal 解析请求头中的 Bearer Token 并写入 SecurityContext。
     * 它使用 Java Servlet 的请求、响应和过滤器链对象，在本项目中让受保护接口能识别当前登录用户。
     *
     * @param request Java Servlet 自带的 HttpServletRequest，表示当前 HTTP 请求。
     * @param response Java Servlet 自带的 HttpServletResponse，表示当前 HTTP 响应。
     * @param filterChain Java Servlet 自带的过滤器链，用于继续执行后续过滤器和 Controller。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length());
        try {
            Long userId = jwtService.parseUserId(token);
            CurrentUser currentUser = userAccessService.loadCurrentUser(userId);
            List<SimpleGrantedAuthority> authorities = currentUser.permissions().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            // 教学重点：JWT 只证明“请求者是谁”，真正的用户状态和权限仍从数据库加载，避免旧 Token 携带过期权限。
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(currentUser, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ignored) {
            // 无效 Token 不在过滤器里直接写响应，交给后续 Security 入口统一返回 401 的 ApiResponse。
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
