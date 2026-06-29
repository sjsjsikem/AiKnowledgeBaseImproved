package com.aiknowledgebase.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Role 是 roles 表对应的 MyBatis-Plus 实体。
 * 它表示系统角色，在本项目中作为“用户拥有哪些权限”的中间层，避免直接把权限绑定到用户。
 */
@Getter
@Setter
@TableName("roles")
public class Role {

    // id 使用 MyBatis-Plus 自带 @TableId 和数据库自增策略，作为 roles 表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // code 是角色编码，例如 ADMIN、USER，用于后端权限判断和前端展示。
    private String code;

    // name 是角色名称，用于管理员后台展示。
    private String name;

    // description 是角色说明，用于帮助学习者理解角色职责。
    private String description;

    // status 表示角色是否启用，当前 Stage 2 使用 ENABLED/DISABLED。
    private String status;

    // deleted 使用 MyBatis-Plus 自带逻辑删除注解，避免角色被物理删除后破坏历史关系。
    @TableLogic
    private Integer deleted;

    // createdAt 记录角色创建时间，用于后台审计和排序。
    private LocalDateTime createdAt;

    // updatedAt 记录角色更新时间，用于后台审计。
    private LocalDateTime updatedAt;
}
