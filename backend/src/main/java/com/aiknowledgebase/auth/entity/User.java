package com.aiknowledgebase.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * User 是 users 表对应的 MyBatis-Plus 实体。
 * 它通过 TableName、TableId、TableLogic 等 MyBatis-Plus 注解映射数据库字段，在本项目中承载账号认证的持久化数据。
 */
@Getter
@Setter
@TableName("users")
public class User {

    // id 使用 MyBatis-Plus 自带的 @TableId 和数据库自增策略，作为 users 表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // username 是账号登录名，由 AuthService 在注册和登录时读写。
    private String username;

    // passwordHash 保存 Spring Security BCrypt 生成的密码哈希，不保存明文密码。
    private String passwordHash;

    // nickname 是用户昵称，用于前端个人资料展示。
    private String nickname;

    // email 是用户邮箱，用于资料展示和后续通知能力扩展。
    private String email;

    // status 表示账号状态，Stage 1 使用 ENABLED 判断账号是否允许登录。
    private String status;

    // deleted 使用 MyBatis-Plus 自带逻辑删除注解，后续删除用户时避免物理删除业务数据。
    @TableLogic
    private Integer deleted;

    // createdAt 记录用户创建时间，用于审计和后台管理排序。
    private LocalDateTime createdAt;

    // updatedAt 记录用户更新时间，用于后续资料变更和审计。
    private LocalDateTime updatedAt;
}
