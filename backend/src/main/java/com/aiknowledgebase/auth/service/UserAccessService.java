package com.aiknowledgebase.auth.service;

import com.aiknowledgebase.auth.entity.User;
import com.aiknowledgebase.auth.mapper.UserMapper;
import com.aiknowledgebase.common.BusinessException;
import com.aiknowledgebase.common.ErrorCode;
import com.aiknowledgebase.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserAccessService 负责把 JWT 中的用户 ID 转换为当前请求用户。
 * 它通过 UserMapper 查询 users 表并组装 CurrentUser，在本项目中为 Spring Security 过滤器提供登录态和权限上下文。
 */
@Service
public class UserAccessService {

    private static final String ENABLED = "ENABLED";

    private final UserMapper userMapper;

    /**
     * 构造方法由 Spring 注入用户数据访问对象。
     * 它把 UserMapper.java 提供的查库能力交给当前服务，用于每次请求加载用户状态。
     *
     * @param userMapper 来自 UserMapper.java，用于根据用户 ID 查询 users 表。
     */
    public UserAccessService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * loadCurrentUser 根据 JWT 解析出的用户 ID 加载当前用户。
     * 它通过 UserMapper 查询数据库并校验账号状态，在本项目中避免已禁用用户继续使用旧 Token 访问接口。
     *
     * @param userId 来自 JwtService.java 解析 JWT subject 得到的用户主键。
     * @return CurrentUser 来自 CurrentUser.java，用于写入 Spring Security 的 SecurityContext。
     */
    public CurrentUser loadCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !ENABLED.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // Stage 2 才会接入真实 RBAC 表。Stage 1 先返回默认 USER 角色，保证认证闭环可用。
        return new CurrentUser(user.getId(), user.getUsername(), List.of("USER"), List.of());
    }
}
