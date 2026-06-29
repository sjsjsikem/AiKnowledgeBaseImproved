package com.aiknowledgebase.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreateRoleRequest 是创建角色接口的请求 DTO。
 * 它使用 Spring Validation 自带注解校验角色编码和名称，在本项目中作为管理员新增角色的输入边界。
 *
 * @param code 前端传入的角色编码，用于后端 RBAC 判断和前端展示。
 * @param name 前端传入的角色名称，用于管理员后台展示。
 * @param description 前端传入的角色说明，用于解释角色职责。
 */
public record CreateRoleRequest(
        @NotBlank
        @Size(max = 64)
        String code,

        @NotBlank
        @Size(max = 64)
        String name,

        @Size(max = 255)
        String description
) {
}
