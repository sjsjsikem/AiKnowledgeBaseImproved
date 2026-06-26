package com.aiknowledgebase.security;

import com.aiknowledgebase.common.BusinessException;
import com.aiknowledgebase.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityUtils 是读取当前登录用户的安全工具类。
 * 它使用 Spring Security 自带的 SecurityContextHolder，在本项目中让业务层可以统一获取 JwtAuthenticationFilter 写入的 CurrentUser。
 */
public final class SecurityUtils {

    /**
     * 私有构造方法阻止工具类被实例化。
     * 它是 Java 工具类常见写法，在本项目中表明 SecurityUtils 只提供静态方法。
     */
    private SecurityUtils() {
    }

    /**
     * currentUser 从 Spring Security 上下文中读取当前用户。
     * 它检查 Authentication.principal 是否为 CurrentUser，在本项目中为 AuthService 等业务代码提供登录用户信息。
     *
     * @return CurrentUser 来自 CurrentUser.java，表示当前请求的登录用户。
     */
    public static CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return currentUser;
    }
}
