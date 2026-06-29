package com.aiknowledgebase.rbac.dto;

/**
 * PermissionResponse 是后台权限列表的响应 DTO。
 * 它从 Permission.java 转换而来，在本项目中用于管理员查看可分配的权限点。
 *
 * @param id 来自 Permission.java 的权限主键。
 * @param code 来自 Permission.java 的权限编码，用于 Spring Security authority。
 * @param name 来自 Permission.java 的权限名称。
 * @param description 来自 Permission.java 的权限说明。
 */
public record PermissionResponse(Long id, String code, String name, String description) {
}
