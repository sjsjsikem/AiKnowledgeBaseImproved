package com.aiknowledgebase.rbac.dto;

import java.util.List;

/**
 * RoleResponse 是后台角色列表的响应 DTO。
 * 它从 Role.java 和角色权限关系组装而来，在本项目中用于展示角色及其拥有的权限。
 *
 * @param id 来自 Role.java 的角色主键。
 * @param code 来自 Role.java 的角色编码。
 * @param name 来自 Role.java 的角色名称。
 * @param description 来自 Role.java 的角色说明。
 * @param status 来自 Role.java 的角色状态。
 * @param permissions 当前角色拥有的权限编码列表。
 */
public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        String status,
        List<String> permissions
) {
}
