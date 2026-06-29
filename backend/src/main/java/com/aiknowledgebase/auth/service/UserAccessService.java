package com.aiknowledgebase.auth.service;

import com.aiknowledgebase.auth.entity.User;
import com.aiknowledgebase.auth.mapper.UserMapper;
import com.aiknowledgebase.common.BusinessException;
import com.aiknowledgebase.common.ErrorCode;
import com.aiknowledgebase.rbac.service.RbacService;
import com.aiknowledgebase.security.CurrentUser;
import org.springframework.stereotype.Service;

/**
 * UserAccessService 负责把 JWT 中的用户 ID 转换为当前请求用户。
 * 它通过 UserMapper 查询用户状态并通过 RbacService 加载角色权限，在本项目中为 Spring Security 过滤器提供登录态和权限上下文。
 */
@Service
public class UserAccessService {

    // ENABLED 是 users.status 的启用状态值，用于 JWT 请求加载当前用户时校验账号是否可用。
    private static final String ENABLED = "ENABLED";

    private final UserMapper userMapper;
    private final RbacService rbacService;

    /**
     * 构造方法由 Spring 注入用户数据访问对象。
     * 它把 UserMapper.java 和 RbacService.java 的能力交给当前服务，用于每次请求加载用户状态和权限。
     *
     * @param userMapper 来自 UserMapper.java，用于根据用户 ID 查询 users 表。
     * @param rbacService 来自 RbacService.java，用于根据用户 ID 查询角色和权限。
     */
    public UserAccessService(UserMapper userMapper, RbacService rbacService) {
        this.userMapper = userMapper;
        this.rbacService = rbacService;
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

        return new CurrentUser(user.getId(), user.getUsername(), rbacService.loadRoleCodes(userId), rbacService.loadPermissionCodes(userId));
    }
}
