import { FormEvent, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createRole,
  fetchAdminUsers,
  fetchPermissions,
  fetchRoles,
  updateRolePermissions,
  updateUserRoles,
  updateUserStatus,
} from '../api/admin';
import { ApiError } from '../api/client';
import type { AdminUser, Permission, Role } from '../types/api';

/**
 * AdminPage 是 Stage 2 的管理员后台页面。
 * 它使用 admin.ts 的后台 API、TanStack Query 和 RBAC 类型，在本项目中演示用户启停、角色分配和角色权限分配。
 */
export function AdminPage() {
  // queryClient 来自 TanStack Query，用于主动刷新后台用户、角色和权限缓存。
  const queryClient = useQueryClient();
  // roleCode 使用 React 自带 useState 保存创建角色表单中的角色编码。
  const [roleCode, setRoleCode] = useState('');
  // roleName 使用 React 自带 useState 保存创建角色表单中的角色名称。
  const [roleName, setRoleName] = useState('');
  // roleDescription 使用 React 自带 useState 保存创建角色表单中的角色说明。
  const [roleDescription, setRoleDescription] = useState('');
  // error 使用 React 自带 useState 保存后台操作失败消息，并在页面顶部展示给管理员。
  const [error, setError] = useState('');

  // usersQuery 使用 TanStack Query 的 useQuery，并调用 admin.ts 中的 fetchAdminUsers。
  // 它在本项目中负责缓存和刷新管理员后台的用户分页数据。
  const usersQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，用于标识后台用户列表缓存。
    queryKey: ['admin-users'],
    // queryFn 调用 fetchAdminUsers，在本项目中把页面查询动作连接到 AdminRbacController.java。
    queryFn: () => fetchAdminUsers(1, 20),
  });
  // rolesQuery 使用 TanStack Query 的 useQuery，并调用 admin.ts 中的 fetchRoles。
  // 它在本项目中为用户角色分配和角色权限展示提供角色字典。
  const rolesQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，用于标识后台角色列表缓存。
    queryKey: ['admin-roles'],
    // queryFn 调用 fetchRoles，从后端 /admin/roles 读取角色数据。
    queryFn: fetchRoles,
  });
  // permissionsQuery 使用 TanStack Query 的 useQuery，并调用 admin.ts 中的 fetchPermissions。
  // 它在本项目中为角色权限分配提供权限字典。
  const permissionsQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，用于标识后台权限列表缓存。
    queryKey: ['admin-permissions'],
    // queryFn 调用 fetchPermissions，从后端 /admin/permissions 读取权限数据。
    queryFn: fetchPermissions,
  });

  // refreshAdminData 是页面级缓存刷新函数。
  // 它通过 queryClient.invalidateQueries 让用户、角色、权限在写操作后重新从后端加载。
  const refreshAdminData = () => {
    queryClient.invalidateQueries({ queryKey: ['admin-users'] });
    queryClient.invalidateQueries({ queryKey: ['admin-roles'] });
    queryClient.invalidateQueries({ queryKey: ['admin-permissions'] });
  };

  // userStatusMutation 使用 TanStack Query 的 useMutation，并调用 admin.ts 中的 updateUserStatus。
  // 它在本项目中把启用/禁用按钮操作转换为后端用户状态更新请求。
  const userStatusMutation = useMutation({
    // mutationFn 接收页面传入的用户 ID 和状态，并调用 AdminRbacController.java 的用户启停接口。
    mutationFn: ({ userId, status }: { userId: number; status: 'ENABLED' | 'DISABLED' }) =>
      updateUserStatus(userId, { status }),
    // onSuccess 来自 TanStack Query mutation 配置，用于操作成功后刷新后台数据。
    onSuccess: refreshAdminData,
    // onError 来自 TanStack Query mutation 配置，用于把失败统一交给 handleMutationError 展示。
    onError: handleMutationError,
  });

  // userRolesMutation 使用 TanStack Query 的 useMutation，并调用 admin.ts 中的 updateUserRoles。
  // 它在本项目中把用户角色复选框结果转换为 user_roles 表更新请求。
  const userRolesMutation = useMutation({
    // mutationFn 接收用户 ID 和角色 ID 列表，并调用后端用户角色分配接口。
    mutationFn: ({ userId, roleIds }: { userId: number; roleIds: number[] }) =>
      updateUserRoles(userId, { roleIds }),
    // onSuccess 操作成功后刷新用户、角色和权限展示数据。
    onSuccess: refreshAdminData,
    // onError 操作失败后统一展示 ApiError 或通用错误文案。
    onError: handleMutationError,
  });

  // createRoleMutation 使用 TanStack Query 的 useMutation，并调用 admin.ts 中的 createRole。
  // 它在本项目中把创建角色表单提交转换为 roles 表新增请求。
  const createRoleMutation = useMutation({
    // mutationFn 直接复用 createRole API 方法，把表单 payload 发送到后端。
    mutationFn: createRole,
    // onSuccess 清空角色表单并刷新后台数据，保证新角色立即出现在页面和授权选项中。
    onSuccess: () => {
      setRoleCode('');
      setRoleName('');
      setRoleDescription('');
      refreshAdminData();
    },
    // onError 操作失败后统一展示 ApiError 或通用错误文案。
    onError: handleMutationError,
  });

  // rolePermissionsMutation 使用 TanStack Query 的 useMutation，并调用 admin.ts 中的 updateRolePermissions。
  // 它在本项目中把角色权限复选框结果转换为 role_permissions 表更新请求。
  const rolePermissionsMutation = useMutation({
    // mutationFn 接收角色 ID 和权限 ID 列表，并调用后端角色权限分配接口。
    mutationFn: ({ roleId, permissionIds }: { roleId: number; permissionIds: number[] }) =>
      updateRolePermissions(roleId, { permissionIds }),
    // onSuccess 操作成功后刷新角色和权限展示数据。
    onSuccess: refreshAdminData,
    // onError 操作失败后统一展示 ApiError 或通用错误文案。
    onError: handleMutationError,
  });

  // users 是从 usersQuery 中派生出的用户列表，接口尚未返回时使用空数组保证 JSX 可安全渲染。
  const users = usersQuery.data?.items ?? [];
  // roles 是从 rolesQuery 中派生出的角色列表，用于用户授权和角色卡片渲染。
  const roles = rolesQuery.data ?? [];
  // permissions 是从 permissionsQuery 中派生出的权限列表，用于角色授权和权限字典展示。
  const permissions = permissionsQuery.data ?? [];
  // loading 汇总三个查询的加载状态，在本项目中控制后台面板的加载提示。
  const loading = usersQuery.isLoading || rolesQuery.isLoading || permissionsQuery.isLoading;
  // queryError 汇总三个查询的错误对象，用于页面顶部统一展示后台数据加载失败。
  const queryError = usersQuery.error ?? rolesQuery.error ?? permissionsQuery.error;

  /**
   * handleMutationError 统一处理后台操作失败。
   * 它接收 TanStack Query mutation 抛出的错误，在本项目中把 ApiError 消息展示到管理员页面顶部。
   *
   * @param err 来自 admin.ts API 调用失败时抛出的错误对象。
   */
  function handleMutationError(err: unknown) {
    setError(err instanceof ApiError ? err.message : '后台操作失败');
  }

  /**
   * handleCreateRole 处理创建角色表单提交。
   * 它使用 React FormEvent 阻止默认刷新，并调用 createRoleMutation 写入后端 roles 表。
   *
   * @param event React 提供的表单提交事件，类型来自 React 的 FormEvent。
   */
  function handleCreateRole(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    createRoleMutation.mutate({
      code: roleCode.trim(),
      name: roleName.trim(),
      description: roleDescription.trim() || undefined,
    });
  }

  return (
    <section className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Admin</p>
          <h1>管理员后台</h1>
        </div>
      </div>

      {(error || queryError) && (
        <p className="form-error">{error || (queryError instanceof ApiError ? queryError.message : '后台数据加载失败')}</p>
      )}

      <div className="admin-grid">
        <section className="admin-panel admin-panel-wide">
          <div className="panel-title">
            <div>
              <p className="eyebrow">Users</p>
              <h2>用户管理</h2>
            </div>
            <span>{loading ? '加载中' : `${usersQuery.data?.total ?? 0} 个用户`}</span>
          </div>
          <div className="admin-list">
            {users.map((user) => (
              <UserRow
                key={user.id}
                user={user}
                roles={roles}
                onStatusChange={(status) => userStatusMutation.mutate({ userId: user.id, status })}
                onRolesChange={(roleIds) => userRolesMutation.mutate({ userId: user.id, roleIds })}
              />
            ))}
          </div>
        </section>

        <section className="admin-panel">
          <div className="panel-title">
            <div>
              <p className="eyebrow">Roles</p>
              <h2>角色管理</h2>
            </div>
          </div>
          <form className="compact-form" onSubmit={handleCreateRole}>
            <input value={roleCode} onChange={(event) => setRoleCode(event.target.value)} placeholder="ROLE_CODE" required />
            <input value={roleName} onChange={(event) => setRoleName(event.target.value)} placeholder="角色名称" required />
            <input value={roleDescription} onChange={(event) => setRoleDescription(event.target.value)} placeholder="角色说明" />
            <button type="submit" disabled={createRoleMutation.isPending}>创建角色</button>
          </form>
          <div className="admin-list">
            {roles.map((role) => (
              <RoleCard
                key={role.id}
                role={role}
                permissions={permissions}
                onPermissionsChange={(permissionIds) =>
                  rolePermissionsMutation.mutate({ roleId: role.id, permissionIds })}
              />
            ))}
          </div>
        </section>

        <section className="admin-panel">
          <div className="panel-title">
            <div>
              <p className="eyebrow">Permissions</p>
              <h2>权限字典</h2>
            </div>
          </div>
          <div className="permission-list">
            {permissions.map((permission) => (
              <article key={permission.id}>
                <strong>{permission.code}</strong>
                <span>{permission.name}</span>
                <p>{permission.description}</p>
              </article>
            ))}
          </div>
        </section>
      </div>
    </section>
  );
}

/**
 * UserRow 是后台用户列表中的单个用户组件。
 * 它接收 AdminUser 和 Role 列表，在本项目中把用户状态切换和角色复选框转换为后台 API 请求参数。
 *
 * @param user 来自 AdminUserResponse.java 的后台用户数据。
 * @param roles 来自 RoleResponse.java 的角色列表。
 * @param onStatusChange 父组件传入的状态更新方法，会调用 updateUserStatus API。
 * @param onRolesChange 父组件传入的角色更新方法，会调用 updateUserRoles API。
 */
function UserRow({
  user,
  roles,
  onStatusChange,
  onRolesChange,
}: {
  user: AdminUser;
  roles: Role[];
  onStatusChange: (status: 'ENABLED' | 'DISABLED') => void;
  onRolesChange: (roleIds: number[]) => void;
}) {
  // selectedRoleIds 根据当前用户已有角色编码和角色字典计算已勾选的角色 ID。
  // 它在本项目中把 AdminUser.roles 转换为后端 UpdateUserRolesRequest 需要的 roleIds。
  const selectedRoleIds = roles.filter((role) => user.roles.includes(role.code)).map((role) => role.id);

  /**
   * toggleRole 根据复选框状态计算新的角色 ID 列表。
   * 它使用 Role.id 和 AdminUser.roles 的映射关系，在本项目中把前端勾选结果转换为后端 UpdateUserRolesRequest。
   *
   * @param roleId 来自 Role.id 的角色主键。
   * @param checked 浏览器 checkbox 提供的选中状态。
   */
  function toggleRole(roleId: number, checked: boolean) {
    // nextRoleIds 使用当前勾选状态计算下一份角色 ID 列表。
    // 它通过新增或移除 roleId，把浏览器 checkbox 状态转换为后端请求体。
    const nextRoleIds = checked
      ? Array.from(new Set([...selectedRoleIds, roleId]))
      : selectedRoleIds.filter((id) => id !== roleId);
    onRolesChange(nextRoleIds);
  }

  return (
    <article className="admin-row">
      <div>
        <strong>{user.nickname}</strong>
        <span>{user.username} · {user.status}</span>
        <small>{user.permissions.length ? user.permissions.join(', ') : '暂无权限'}</small>
      </div>
      <div className="inline-actions">
        <button
          type="button"
          onClick={() => onStatusChange(user.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')}
        >
          {user.status === 'ENABLED' ? '禁用' : '启用'}
        </button>
      </div>
      <div className="check-list">
        {roles.map((role) => (
          <label key={role.id}>
            <input
              type="checkbox"
              checked={user.roles.includes(role.code)}
              onChange={(event) => toggleRole(role.id, event.target.checked)}
            />
            {role.code}
          </label>
        ))}
      </div>
    </article>
  );
}

/**
 * RoleCard 是后台角色列表中的单个角色组件。
 * 它接收 Role 和 Permission 列表，在本项目中把权限复选框转换为角色权限分配请求。
 *
 * @param role 来自 RoleResponse.java 的角色数据。
 * @param permissions 来自 PermissionResponse.java 的权限字典。
 * @param onPermissionsChange 父组件传入的权限更新方法，会调用 updateRolePermissions API。
 */
function RoleCard({
  role,
  permissions,
  onPermissionsChange,
}: {
  role: Role;
  permissions: Permission[];
  onPermissionsChange: (permissionIds: number[]) => void;
}) {
  // selectedPermissionIds 根据当前角色已有权限编码和权限字典计算已勾选的权限 ID。
  // 它在本项目中把 Role.permissions 转换为后端 UpdateRolePermissionsRequest 需要的 permissionIds。
  const selectedPermissionIds = permissions
    .filter((permission) => role.permissions.includes(permission.code))
    .map((permission) => permission.id);

  /**
   * togglePermission 根据复选框状态计算新的权限 ID 列表。
   * 它使用 Permission.id 和 Role.permissions 的映射关系，在本项目中把前端勾选结果转换为后端 UpdateRolePermissionsRequest。
   *
   * @param permissionId 来自 Permission.id 的权限主键。
   * @param checked 浏览器 checkbox 提供的选中状态。
   */
  function togglePermission(permissionId: number, checked: boolean) {
    // nextPermissionIds 使用当前勾选状态计算下一份权限 ID 列表。
    // 它通过新增或移除 permissionId，把浏览器 checkbox 状态转换为后端请求体。
    const nextPermissionIds = checked
      ? Array.from(new Set([...selectedPermissionIds, permissionId]))
      : selectedPermissionIds.filter((id) => id !== permissionId);
    onPermissionsChange(nextPermissionIds);
  }

  return (
    <article className="role-card">
      <strong>{role.code}</strong>
      <span>{role.name}</span>
      <p>{role.description}</p>
      <div className="check-list">
        {permissions.map((permission) => (
          <label key={permission.id}>
            <input
              type="checkbox"
              checked={role.permissions.includes(permission.code)}
              onChange={(event) => togglePermission(permission.id, event.target.checked)}
            />
            {permission.code}
          </label>
        ))}
      </div>
    </article>
  );
}
