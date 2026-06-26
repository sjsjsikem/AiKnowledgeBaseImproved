package com.aiknowledgebase.auth.dto;

import java.util.List;

/**
 * UserProfile 是返回给前端的安全用户资料 DTO。
 * 它从 User.java 实体转换而来但不包含 passwordHash，在本项目中避免把数据库敏感字段直接暴露给浏览器。
 *
 * @param id 来自 User.java 的用户主键，用于前端识别当前账号。
 * @param username 来自 User.java 的登录名，用于展示和审计。
 * @param nickname 来自 User.java 的昵称，用于页面显示。
 * @param email 来自 User.java 的邮箱，用于资料展示。
 * @param status 来自 User.java 的账号状态，用于判断账号是否可用。
 * @param roles 当前用户角色列表，Stage 1 由 AuthService/UserAccessService 默认生成，Stage 2 后来自 RBAC 表。
 * @param permissions 当前用户权限列表，Stage 2 后由 RBAC 权限关系加载。
 */
public record UserProfile(
        Long id,
        String username,
        String nickname,
        String email,
        String status,
        List<String> roles,
        List<String> permissions
) {
}
