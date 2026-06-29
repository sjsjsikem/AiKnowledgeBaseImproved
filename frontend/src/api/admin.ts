import { apiClient, unwrap } from './client';
import type {
  AdminUser,
  CreateRolePayload,
  PageResponse,
  Permission,
  Role,
  UpdateRolePermissionsPayload,
  UpdateUserRolesPayload,
  UpdateUserStatusPayload,
} from '../types/api';

/**
 * fetchAdminUsers 查询后台用户分页列表。
 * 它调用 AdminRbacController.java 的 /admin/users，在本项目中为管理员用户管理页面提供数据。
 *
 * @param page 当前页码，来自 AdminPage.tsx 的分页状态。
 * @param pageSize 每页数量，来自 AdminPage.tsx 的分页状态。
 * @returns PageResponse<AdminUser>，包含用户、角色和权限。
 */
export function fetchAdminUsers(page = 1, pageSize = 10) {
  return unwrap<PageResponse<AdminUser>>(apiClient.get('/admin/users', { params: { page, pageSize } }));
}

/**
 * updateUserStatus 更新后台用户启停状态。
 * 它调用 AdminRbacController.java 的用户状态接口，在本项目中让管理员禁用或启用账号。
 *
 * @param userId 来自 AdminUser.id 的用户主键。
 * @param payload 前端选择的 ENABLED 或 DISABLED 状态。
 * @returns AdminUser，表示更新后的用户资料。
 */
export function updateUserStatus(userId: number, payload: UpdateUserStatusPayload) {
  return unwrap<AdminUser>(apiClient.patch(`/admin/users/${userId}/status`, payload));
}

/**
 * updateUserRoles 更新后台用户角色关系。
 * 它调用 AdminRbacController.java 的用户角色接口，在本项目中让管理员给用户分配角色。
 *
 * @param userId 来自 AdminUser.id 的用户主键。
 * @param payload 前端选择的角色 ID 列表。
 * @returns AdminUser，表示更新后的用户资料。
 */
export function updateUserRoles(userId: number, payload: UpdateUserRolesPayload) {
  return unwrap<AdminUser>(apiClient.put(`/admin/users/${userId}/roles`, payload));
}

/**
 * fetchRoles 查询后台角色列表。
 * 它调用 AdminRbacController.java 的 /admin/roles，在本项目中为用户角色分配和角色权限分配提供选项。
 *
 * @returns Role 列表，包含每个角色拥有的权限编码。
 */
export function fetchRoles() {
  return unwrap<Role[]>(apiClient.get('/admin/roles'));
}

/**
 * createRole 创建后台角色。
 * 它调用 AdminRbacController.java 的角色创建接口，在本项目中支持管理员扩展新的业务角色。
 *
 * @param payload 来自 AdminPage.tsx 的角色创建表单。
 * @returns Role，表示新创建的角色。
 */
export function createRole(payload: CreateRolePayload) {
  return unwrap<Role>(apiClient.post('/admin/roles', payload));
}

/**
 * updateRolePermissions 更新角色权限关系。
 * 它调用 AdminRbacController.java 的角色权限接口，在本项目中让管理员给角色分配权限。
 *
 * @param roleId 来自 Role.id 的角色主键。
 * @param payload 前端选择的权限 ID 列表。
 * @returns Role，表示更新后的角色。
 */
export function updateRolePermissions(roleId: number, payload: UpdateRolePermissionsPayload) {
  return unwrap<Role>(apiClient.put(`/admin/roles/${roleId}/permissions`, payload));
}

/**
 * fetchPermissions 查询后台权限列表。
 * 它调用 AdminRbacController.java 的 /admin/permissions，在本项目中为角色权限分配提供权限字典。
 *
 * @returns Permission 列表，包含权限编码、名称和说明。
 */
export function fetchPermissions() {
  return unwrap<Permission[]>(apiClient.get('/admin/permissions'));
}
