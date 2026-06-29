package com.aiknowledgebase.rbac.mapper;

import com.aiknowledgebase.rbac.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * RoleMapper 是 roles 表的数据访问入口。
 * 它继承 MyBatis-Plus 自带 BaseMapper，并补充按用户查询角色的 SQL，在本项目中支撑当前用户权限加载和后台角色管理。
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * selectEnabledByUserId 查询某个用户拥有的启用角色。
     * 它通过 user_roles 连接 users 和 roles，在本项目中为 JWT 请求加载当前用户角色列表。
     *
     * @param userId 来自 User.java 的用户主键。
     * @return Role 列表，表示当前用户拥有的启用角色。
     */
    @Select("""
            SELECT r.*
            FROM roles r
            JOIN user_roles ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.deleted = 0
              AND r.status = 'ENABLED'
            ORDER BY r.code
            """)
    List<Role> selectEnabledByUserId(@Param("userId") Long userId);
}
