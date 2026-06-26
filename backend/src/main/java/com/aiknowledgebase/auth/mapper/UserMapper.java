package com.aiknowledgebase.auth.mapper;

import com.aiknowledgebase.auth.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * UserMapper 是 users 表的数据访问入口。
 * 它继承 MyBatis-Plus 自带的 BaseMapper<User>，在本项目中为 AuthService 和 UserAccessService 提供增删改查能力。
 */
public interface UserMapper extends BaseMapper<User> {
}
