package com.aiknowledgebase.rbac.dto;

import java.util.List;

/**
 * AdminUserResponse 是管理员用户列表的响应 DTO。
 * 它从 User.java 和 RBAC 查询结果组装而来，在本项目中避免把 passwordHash 等敏感实体字段返回给前端。
 *
 * @param id 来自 User.java 的用户主键。
 * @param username 来自 User.java 的登录用户名。
 * @param nickname 来自 User.java 的用户昵称。
 * @param email 来自 User.java 的邮箱。
 * @param status 来自 User.java 的账号状态。
 * @param roles 来自 roles 表的角色编码列表。
 * @param permissions 来自 permissions 表的权限编码列表。
 */
public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String status,
        List<String> roles,
        List<String> permissions
) {
}
