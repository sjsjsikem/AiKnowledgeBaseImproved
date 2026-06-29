package com.aiknowledgebase.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * RolePermission 是 role_permissions 表对应的角色权限关系实体。
 * 它连接 Role.java 和 Permission.java，在本项目中实现“一个角色可以拥有多个权限”的 RBAC 关系。
 */
@Getter
@Setter
@TableName("role_permissions")
public class RolePermission {

    // id 使用 MyBatis-Plus 自带 @TableId 和数据库自增策略，作为关系表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // roleId 来自 roles 表主键，用于定位被授权角色。
    private Long roleId;

    // permissionId 来自 permissions 表主键，用于定位授予角色的权限。
    private Long permissionId;

    // createdAt 记录关系创建时间，用于后台审计。
    private LocalDateTime createdAt;
}
