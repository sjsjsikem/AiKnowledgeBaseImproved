package com.aiknowledgebase.rbac.mapper;

import com.aiknowledgebase.rbac.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * UserRoleMapper 是 user_roles 表的数据访问入口。
 * 它继承 MyBatis-Plus 自带 BaseMapper，在本项目中负责用户和角色关系的新增、删除和查询。
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
