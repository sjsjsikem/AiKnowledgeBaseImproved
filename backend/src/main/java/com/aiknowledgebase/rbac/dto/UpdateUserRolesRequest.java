package com.aiknowledgebase.rbac.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * UpdateUserRolesRequest 是用户角色分配接口的请求 DTO。
 * 它接收角色 ID 列表，在本项目中让管理员通过后台重建某个用户的角色关系。
 *
 * @param roleIds 前端选择的角色 ID 列表，来自 RoleResponse.id。
 */
public record UpdateUserRolesRequest(@NotNull List<Long> roleIds) {
}
