package com.aiknowledgebase.rbac.service;

import com.aiknowledgebase.auth.entity.User;
import com.aiknowledgebase.auth.mapper.UserMapper;
import com.aiknowledgebase.common.BusinessException;
import com.aiknowledgebase.common.ErrorCode;
import com.aiknowledgebase.common.PageResponse;
import com.aiknowledgebase.rbac.dto.AdminUserResponse;
import com.aiknowledgebase.rbac.dto.CreateRoleRequest;
import com.aiknowledgebase.rbac.dto.PermissionResponse;
import com.aiknowledgebase.rbac.dto.RoleResponse;
import com.aiknowledgebase.rbac.dto.UpdateRolePermissionsRequest;
import com.aiknowledgebase.rbac.dto.UpdateUserRolesRequest;
import com.aiknowledgebase.rbac.dto.UpdateUserStatusRequest;
import com.aiknowledgebase.rbac.entity.Permission;
import com.aiknowledgebase.rbac.entity.Role;
import com.aiknowledgebase.rbac.entity.RolePermission;
import com.aiknowledgebase.rbac.entity.UserRole;
import com.aiknowledgebase.rbac.mapper.PermissionMapper;
import com.aiknowledgebase.rbac.mapper.RoleMapper;
import com.aiknowledgebase.rbac.mapper.RolePermissionMapper;
import com.aiknowledgebase.rbac.mapper.UserRoleMapper;
import com.aiknowledgebase.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RbacService 负责角色、权限和授权关系的业务编排。
 * 它连接 users、roles、permissions、user_roles、role_permissions 五类数据，在本项目中同时服务登录鉴权和管理员后台。
 */
@Service
public class RbacService {

    // ENABLED 是 users.status 和 roles.status 的启用状态值，用于判断账号和角色是否可用。
    private static final String ENABLED = "ENABLED";
    // DISABLED 是 users.status 的禁用状态值，用于管理员后台停用账号。
    private static final String DISABLED = "DISABLED";
    // DEFAULT_USER_ROLE 是注册用户默认角色编码，用于把新用户接入 RBAC 权限体系。
    private static final String DEFAULT_USER_ROLE = "USER";

    // userMapper 来自 UserMapper.java，用于后台用户列表和用户启停。
    private final UserMapper userMapper;
    // roleMapper 来自 RoleMapper.java，用于角色 CRUD 和按用户查询角色。
    private final RoleMapper roleMapper;
    // permissionMapper 来自 PermissionMapper.java，用于权限列表和按用户查询权限。
    private final PermissionMapper permissionMapper;
    // userRoleMapper 来自 UserRoleMapper.java，用于维护用户角色关系。
    private final UserRoleMapper userRoleMapper;
    // rolePermissionMapper 来自 RolePermissionMapper.java，用于维护角色权限关系。
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 构造方法由 Spring 注入 RBAC 需要的 Mapper。
     * 它把用户、角色、权限和两张关系表的数据访问能力组合到一个业务服务中。
     *
     * @param userMapper 来自 UserMapper.java，用于查询和更新 users 表。
     * @param roleMapper 来自 RoleMapper.java，用于查询和写入 roles 表。
     * @param permissionMapper 来自 PermissionMapper.java，用于查询 permissions 表。
     * @param userRoleMapper 来自 UserRoleMapper.java，用于维护 user_roles 表。
     * @param rolePermissionMapper 来自 RolePermissionMapper.java，用于维护 role_permissions 表。
     */
    public RbacService(
            UserMapper userMapper,
            RoleMapper roleMapper,
            PermissionMapper permissionMapper,
            UserRoleMapper userRoleMapper,
            RolePermissionMapper rolePermissionMapper
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    /**
     * loadRoleCodes 查询用户拥有的角色编码。
     * 它调用 RoleMapper.java 的跨表查询，在本项目中为 UserProfile 和 CurrentUser 提供角色列表。
     *
     * @param userId 来自 User.java 的用户主键。
     * @return 角色编码列表，例如 ADMIN、USER。
     */
    public List<String> loadRoleCodes(Long userId) {
        return roleMapper.selectEnabledByUserId(userId).stream()
                .map(Role::getCode)
                .toList();
    }

    /**
     * loadPermissionCodes 查询用户通过角色获得的权限编码。
     * 它调用 PermissionMapper.java 的 RBAC 跨表查询，在本项目中把数据库权限转换为 Spring Security authority。
     *
     * @param userId 来自 User.java 的用户主键。
     * @return 权限编码列表，例如 admin:user:read。
     */
    public List<String> loadPermissionCodes(Long userId) {
        return permissionMapper.selectByUserId(userId).stream()
                .map(Permission::getCode)
                .toList();
    }

    /**
     * assignDefaultUserRole 为新注册用户分配默认 USER 角色。
     * 它读取 roles 表中的 USER 角色并写入 user_roles，在本项目中让注册用户进入 RBAC 体系。
     *
     * @param userId 来自 User.java 的新用户主键。
     */
    public void assignDefaultUserRole(Long userId) {
        // role 来自 roles 表中的 USER 角色，用于给新注册用户写入 user_roles 默认关系。
        Role role = findRoleByCode(DEFAULT_USER_ROLE);
        if (role == null) {
            return;
        }
        if (hasUserRole(userId, role.getId())) {
            return;
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRole.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(userRole);
    }

    /**
     * listUsers 分页查询后台用户列表。
     * 它使用 MyBatis-Plus 的 Page 查询 users 表，并为每个用户补充角色和权限，在本项目中支撑管理员用户管理页。
     *
     * @param page 当前页码，来自前端分页参数。
     * @param pageSize 每页数量，来自前端分页参数。
     * @return PageResponse 包装后的后台用户列表。
     */
    public PageResponse<AdminUserResponse> listUsers(long page, long pageSize) {
        // result 使用 MyBatis-Plus 的 Page 查询 users 表，在本项目中承载后台用户分页数据和总数。
        Page<User> result = userMapper.selectPage(
                Page.of(page, pageSize),
                new LambdaQueryWrapper<User>().orderByDesc(User::getId)
        );
        // items 把 User 实体列表转换为 AdminUserResponse，避免 passwordHash 等敏感字段暴露给前端。
        List<AdminUserResponse> items = result.getRecords().stream()
                .map(this::toAdminUserResponse)
                .toList();
        return PageResponse.of(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * updateUserStatus 更新用户启停状态。
     * 它校验状态值、禁止管理员禁用自己并更新 users 表，在本项目中实现后台账号启停。
     *
     * @param userId 来自路径参数的用户主键。
     * @param request 来自 UpdateUserStatusRequest.java，承载 ENABLED 或 DISABLED。
     * @return AdminUserResponse 更新后的用户资料。
     */
    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        // status 来自 UpdateUserStatusRequest.java，用于更新 users.status。
        String status = request.status();
        if (!ENABLED.equals(status) && !DISABLED.equals(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户状态只支持 ENABLED 或 DISABLED");
        }
        if (SecurityUtils.currentUser().id().equals(userId) && DISABLED.equals(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能禁用当前登录账号");
        }
        // user 来自 users 表，用于执行账号启停更新。
        User user = findUser(userId);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return toAdminUserResponse(user);
    }

    /**
     * updateUserRoles 重建某个用户的角色关系。
     * 它先删除 user_roles 中的旧关系，再插入前端提交的角色 ID，在本项目中实现用户角色分配。
     *
     * @param userId 来自路径参数的用户主键。
     * @param request 来自 UpdateUserRolesRequest.java，承载角色 ID 列表。
     * @return AdminUserResponse 更新后的用户资料。
     */
    @Transactional
    public AdminUserResponse updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        if (SecurityUtils.currentUser().id().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能修改当前登录账号的角色");
        }
        // user 来自 users 表，用于确认被授权账号存在。
        User user = findUser(userId);
        // roleIds 来自 UpdateUserRolesRequest.java，去重后用于重建 user_roles 关系。
        List<Long> roleIds = distinctIds(request.roleIds());
        ensureRolesExist(roleIds);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreatedAt(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }
        return toAdminUserResponse(user);
    }

    /**
     * listRoles 查询角色列表。
     * 它读取 roles 表并补充每个角色拥有的权限编码，在本项目中支撑后台角色管理页。
     *
     * @return RoleResponse 列表，供前端展示和分配角色。
     */
    public List<RoleResponse> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>().orderByAsc(Role::getCode)).stream()
                .map(this::toRoleResponse)
                .toList();
    }

    /**
     * createRole 创建新角色。
     * 它校验角色编码唯一后写入 roles 表，在本项目中让管理员可以扩展业务角色。
     *
     * @param request 来自 CreateRoleRequest.java，承载角色编码、名称和说明。
     * @return RoleResponse 新创建的角色响应。
     */
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (findRoleByCode(request.code()) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }
        // role 是即将写入 roles 表的新角色实体，由 CreateRoleRequest.java 转换而来。
        Role role = new Role();
        role.setCode(request.code());
        role.setName(request.name());
        role.setDescription(request.description());
        role.setStatus(ENABLED);
        role.setDeleted(0);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        return toRoleResponse(role);
    }

    /**
     * updateRolePermissions 重建某个角色的权限关系。
     * 它先删除 role_permissions 中的旧关系，再插入前端提交的权限 ID，在本项目中实现角色权限分配。
     *
     * @param roleId 来自路径参数的角色主键。
     * @param request 来自 UpdateRolePermissionsRequest.java，承载权限 ID 列表。
     * @return RoleResponse 更新后的角色响应。
     */
    @Transactional
    public RoleResponse updateRolePermissions(Long roleId, UpdateRolePermissionsRequest request) {
        // role 来自 roles 表，用于确认被授权角色存在。
        Role role = findRole(roleId);
        // permissionIds 来自 UpdateRolePermissionsRequest.java，去重后用于重建 role_permissions 关系。
        List<Long> permissionIds = distinctIds(request.permissionIds());
        ensurePermissionsExist(permissionIds);
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreatedAt(LocalDateTime.now());
            rolePermissionMapper.insert(rolePermission);
        }
        return toRoleResponse(role);
    }

    /**
     * listPermissions 查询全部权限点。
     * 它读取 permissions 表并转换为 DTO，在本项目中支撑角色权限分配 UI。
     *
     * @return PermissionResponse 列表，供前端展示。
     */
    public List<PermissionResponse> listPermissions() {
        return permissionMapper.selectList(new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getCode)).stream()
                .map(this::toPermissionResponse)
                .toList();
    }

    /**
     * toAdminUserResponse 把 User 实体转换为后台用户响应。
     * 它通过额外查询角色和权限去除敏感字段，在本项目中保证 Entity 不直接暴露给前端。
     *
     * @param user 来自 User.java 的数据库实体。
     * @return AdminUserResponse 后台用户响应 DTO。
     */
    private AdminUserResponse toAdminUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getStatus(),
                loadRoleCodes(user.getId()),
                loadPermissionCodes(user.getId())
        );
    }

    /**
     * toRoleResponse 把 Role 实体转换为后台角色响应。
     * 它查询 PermissionMapper.java 补充角色权限编码，在本项目中让前端一次拿到角色和权限关系。
     *
     * @param role 来自 Role.java 的数据库实体。
     * @return RoleResponse 后台角色响应 DTO。
     */
    private RoleResponse toRoleResponse(Role role) {
        List<String> permissions = permissionMapper.selectByRoleId(role.getId()).stream()
                .map(Permission::getCode)
                .toList();
        return new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getDescription(), role.getStatus(), permissions);
    }

    /**
     * toPermissionResponse 把 Permission 实体转换为后台权限响应。
     * 它只暴露权限编码、名称和说明，在本项目中为角色权限分配提供可选项。
     *
     * @param permission 来自 Permission.java 的数据库实体。
     * @return PermissionResponse 后台权限响应 DTO。
     */
    private PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getCode(), permission.getName(), permission.getDescription());
    }

    /**
     * findUser 根据用户 ID 查询 User 实体。
     * 它调用 UserMapper.java 并在不存在时抛出业务异常，在本项目中统一后台用户操作的资源校验。
     *
     * @param userId 来自路径参数的用户主键。
     * @return User 数据库实体。
     */
    private User findUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    /**
     * findRole 根据角色 ID 查询 Role 实体。
     * 它调用 RoleMapper.java 并在不存在时抛出业务异常，在本项目中统一后台角色操作的资源校验。
     *
     * @param roleId 来自路径参数的角色主键。
     * @return Role 数据库实体。
     */
    private Role findRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    /**
     * findRoleByCode 根据角色编码查询 Role 实体。
     * 它使用 MyBatis-Plus 的 LambdaQueryWrapper 查询 roles 表，在本项目中服务默认角色分配和角色编码唯一校验。
     *
     * @param code 角色编码，例如 USER、ADMIN。
     * @return Role 数据库实体；不存在时返回 null。
     */
    private Role findRoleByCode(String code) {
        return roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, code)
                .last("LIMIT 1"));
    }

    /**
     * hasUserRole 判断用户是否已拥有某个角色。
     * 它查询 user_roles 表中的关系记录，在本项目中避免默认角色重复插入。
     *
     * @param userId 来自 User.java 的用户主键。
     * @param roleId 来自 Role.java 的角色主键。
     * @return true 表示关系已存在。
     */
    private boolean hasUserRole(Long userId, Long roleId) {
        return userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId)) > 0;
    }

    /**
     * ensureRolesExist 校验前端提交的角色 ID 是否都存在。
     * 它使用 RoleMapper.java 的 selectByIds 批量查询，在本项目中避免写入无效 user_roles 关系。
     *
     * @param roleIds 来自 UpdateUserRolesRequest.java 的角色 ID 列表。
     */
    private void ensureRolesExist(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }
        if (roleMapper.selectByIds(roleIds).size() != roleIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "存在无效角色");
        }
    }

    /**
     * ensurePermissionsExist 校验前端提交的权限 ID 是否都存在。
     * 它使用 PermissionMapper.java 的 selectByIds 批量查询，在本项目中避免写入无效 role_permissions 关系。
     *
     * @param permissionIds 来自 UpdateRolePermissionsRequest.java 的权限 ID 列表。
     */
    private void ensurePermissionsExist(List<Long> permissionIds) {
        if (permissionIds.isEmpty()) {
            return;
        }
        if (permissionMapper.selectByIds(permissionIds).size() != permissionIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "存在无效权限");
        }
    }

    /**
     * distinctIds 对前端提交的 ID 列表去重并过滤 null。
     * 它使用 Java Stream API 处理集合，在本项目中避免重复关系触发数据库唯一约束错误。
     *
     * @param ids 来自前端请求 DTO 的 ID 列表。
     * @return 去重后的 ID 列表。
     */
    private List<Long> distinctIds(List<Long> ids) {
        return ids.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
    }
}
