package com.aiknowledgebase.rbac.mapper;

import com.aiknowledgebase.rbac.entity.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * RolePermissionMapper 是 role_permissions 表的数据访问入口。
 * 它继承 MyBatis-Plus 自带 BaseMapper，在本项目中负责角色和权限关系的新增、删除和查询。
 */
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
