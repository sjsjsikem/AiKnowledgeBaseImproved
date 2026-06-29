package com.aiknowledgebase.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * UserRole 是 user_roles 表对应的用户角色关系实体。
 * 它连接 User.java 和 Role.java，在本项目中实现“一个用户可以拥有多个角色”的 RBAC 关系。
 */
@Getter
@Setter
@TableName("user_roles")
public class UserRole {

    // id 使用 MyBatis-Plus 自带 @TableId 和数据库自增策略，作为关系表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // userId 来自 users 表主键，用于定位被授权用户。
    private Long userId;

    // roleId 来自 roles 表主键，用于定位授予用户的角色。
    private Long roleId;

    // createdAt 记录关系创建时间，用于后台审计。
    private LocalDateTime createdAt;
}
