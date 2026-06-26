package com.aiknowledgebase.security;

import java.util.List;

/**
 * CurrentUser 是写入 Spring Security 上下文的当前登录用户模型。
 * 它由 UserAccessService.java 从数据库加载后创建，在本项目中让 Controller 和 Service 可以安全获取当前用户身份。
 *
 * @param id 来自 User.java 的用户主键。
 * @param username 来自 User.java 的用户名。
 * @param roles 当前用户角色列表，Stage 1 为默认 USER，Stage 2 后来自 RBAC 表。
 * @param permissions 当前用户权限列表，Stage 2 后由角色权限关系加载。
 */
public record CurrentUser(Long id, String username, List<String> roles, List<String> permissions) {
}
