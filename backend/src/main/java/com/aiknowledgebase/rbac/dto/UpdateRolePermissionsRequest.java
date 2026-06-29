package com.aiknowledgebase.rbac.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * UpdateRolePermissionsRequest 是角色权限分配接口的请求 DTO。
 * 它接收权限 ID 列表，在本项目中让管理员通过后台重建某个角色的权限关系。
 *
 * @param permissionIds 前端选择的权限 ID 列表，来自 PermissionResponse.id。
 */
public record UpdateRolePermissionsRequest(@NotNull List<Long> permissionIds) {
}
