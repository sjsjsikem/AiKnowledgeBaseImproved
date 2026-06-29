package com.aiknowledgebase.rbac.mapper;

import com.aiknowledgebase.rbac.entity.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * PermissionMapper 是 permissions 表的数据访问入口。
 * 它继承 MyBatis-Plus 自带 BaseMapper，并补充按用户和角色查询权限的 SQL，在本项目中支撑接口鉴权和后台权限分配。
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * selectByUserId 查询某个用户通过角色获得的权限。
     * 它连接 user_roles、roles、role_permissions 和 permissions，在本项目中把 RBAC 表关系转换为 Spring Security authority。
     *
     * @param userId 来自 User.java 的用户主键。
     * @return Permission 列表，表示当前用户拥有的权限。
     */
    @Select("""
            SELECT DISTINCT p.*
            FROM permissions p
            JOIN role_permissions rp ON rp.permission_id = p.id
            JOIN roles r ON r.id = rp.role_id
            JOIN user_roles ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.deleted = 0
              AND r.status = 'ENABLED'
              AND p.deleted = 0
            ORDER BY p.code
            """)
    List<Permission> selectByUserId(@Param("userId") Long userId);

    /**
     * selectByRoleId 查询某个角色拥有的权限。
     * 它连接 role_permissions 和 permissions，在本项目中用于管理员后台展示角色权限。
     *
     * @param roleId 来自 Role.java 的角色主键。
     * @return Permission 列表，表示该角色拥有的权限。
     */
    @Select("""
            SELECT p.*
            FROM permissions p
            JOIN role_permissions rp ON rp.permission_id = p.id
            WHERE rp.role_id = #{roleId}
              AND p.deleted = 0
            ORDER BY p.code
            """)
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);
}
