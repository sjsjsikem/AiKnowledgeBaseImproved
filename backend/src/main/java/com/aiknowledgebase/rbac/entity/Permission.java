package com.aiknowledgebase.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Permission 是 permissions 表对应的 MyBatis-Plus 实体。
 * 它表示可授权的动作编码，在本项目中用于 Spring Security 的接口权限判断和前端按钮权限控制。
 */
@Getter
@Setter
@TableName("permissions")
public class Permission {

    // id 使用 MyBatis-Plus 自带 @TableId 和数据库自增策略，作为 permissions 表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // code 是权限编码，例如 admin:user:read，用于后端 hasAuthority 判断。
    private String code;

    // name 是权限名称，用于管理员后台展示。
    private String name;

    // description 是权限说明，用于解释该权限控制的业务动作。
    private String description;

    // deleted 使用 MyBatis-Plus 逻辑删除，避免权限被误删后影响历史角色关系。
    @TableLogic
    private Integer deleted;

    // createdAt 记录权限创建时间，用于审计。
    private LocalDateTime createdAt;

    // updatedAt 记录权限更新时间，用于审计。
    private LocalDateTime updatedAt;
}
