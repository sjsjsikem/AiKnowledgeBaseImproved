package com.aiknowledgebase.rbac.controller;

import com.aiknowledgebase.common.ApiResponse;
import com.aiknowledgebase.common.PageResponse;
import com.aiknowledgebase.rbac.dto.AdminUserResponse;
import com.aiknowledgebase.rbac.dto.CreateRoleRequest;
import com.aiknowledgebase.rbac.dto.PermissionResponse;
import com.aiknowledgebase.rbac.dto.RoleResponse;
import com.aiknowledgebase.rbac.dto.UpdateRolePermissionsRequest;
import com.aiknowledgebase.rbac.dto.UpdateUserRolesRequest;
import com.aiknowledgebase.rbac.dto.UpdateUserStatusRequest;
import com.aiknowledgebase.rbac.service.RbacService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AdminRbacController 是管理员后台 RBAC 接口入口。
 * 它使用 Spring MVC Controller 注解暴露 /admin/users、/admin/roles、/admin/permissions，在本项目中连接前端后台页面和 RbacService。
 */
@RestController
@RequestMapping("/admin")
public class AdminRbacController {

    // rbacService 来自 RbacService.java，负责用户、角色、权限和授权关系的业务编排。
    private final RbacService rbacService;

    /**
     * 构造方法由 Spring 注入 RBAC 业务服务。
     * 它让 Controller 只做 HTTP 参数适配，把权限关系操作交给 Service 层。
     *
     * @param rbacService 来自 RbacService.java，承接 RBAC 管理业务逻辑。
     */
    public AdminRbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    /**
     * listUsers 是后台用户分页接口。
     * 它接收前端分页参数并调用 RbacService 查询用户、角色和权限，在本项目中支撑管理员用户管理。
     *
     * @param page Spring MVC 从 query string 读取的当前页码。
     * @param pageSize Spring MVC 从 query string 读取的每页数量。
     * @return ApiResponse 包装 PageResponse<AdminUserResponse>。
     */
    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserResponse>> listUsers(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(rbacService.listUsers(page, pageSize));
    }

    /**
     * updateUserStatus 是后台用户启停接口。
     * 它接收用户 ID 和状态请求体并调用 RbacService 更新 users 表，在本项目中控制账号是否可继续登录。
     *
     * @param userId Spring MVC 从路径中读取的用户主键。
     * @param request 来自 UpdateUserStatusRequest.java，由 Spring Validation 校验非空。
     * @return ApiResponse 包装更新后的 AdminUserResponse。
     */
    @PatchMapping("/users/{userId}/status")
    public ApiResponse<AdminUserResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.success(rbacService.updateUserStatus(userId, request));
    }

    /**
     * updateUserRoles 是后台用户角色分配接口。
     * 它接收用户 ID 和角色 ID 列表并调用 RbacService 重建 user_roles，在本项目中实现用户授权。
     *
     * @param userId Spring MVC 从路径中读取的用户主键。
     * @param request 来自 UpdateUserRolesRequest.java，承载角色 ID 列表。
     * @return ApiResponse 包装更新后的 AdminUserResponse。
     */
    @PutMapping("/users/{userId}/roles")
    public ApiResponse<AdminUserResponse> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ApiResponse.success(rbacService.updateUserRoles(userId, request));
    }

    /**
     * listRoles 是后台角色列表接口。
     * 它调用 RbacService 查询角色及权限编码，在本项目中支撑角色管理和用户角色分配。
     *
     * @return ApiResponse 包装 RoleResponse 列表。
     */
    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.success(rbacService.listRoles());
    }

    /**
     * createRole 是后台创建角色接口。
     * 它接收 CreateRoleRequest 并调用 RbacService 写入 roles 表，在本项目中支持管理员扩展角色。
     *
     * @param request 来自 CreateRoleRequest.java，由 Spring Validation 校验角色编码和名称。
     * @return ApiResponse 包装新创建的 RoleResponse。
     */
    @PostMapping("/roles")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(rbacService.createRole(request));
    }

    /**
     * updateRolePermissions 是后台角色权限分配接口。
     * 它接收角色 ID 和权限 ID 列表并调用 RbacService 重建 role_permissions，在本项目中实现角色授权。
     *
     * @param roleId Spring MVC 从路径中读取的角色主键。
     * @param request 来自 UpdateRolePermissionsRequest.java，承载权限 ID 列表。
     * @return ApiResponse 包装更新后的 RoleResponse。
     */
    @PutMapping("/roles/{roleId}/permissions")
    public ApiResponse<RoleResponse> updateRolePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        return ApiResponse.success(rbacService.updateRolePermissions(roleId, request));
    }

    /**
     * listPermissions 是后台权限列表接口。
     * 它调用 RbacService 查询权限点，在本项目中为角色权限分配提供可选权限集合。
     *
     * @return ApiResponse 包装 PermissionResponse 列表。
     */
    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> listPermissions() {
        return ApiResponse.success(rbacService.listPermissions());
    }
}
