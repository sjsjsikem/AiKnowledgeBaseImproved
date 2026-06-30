package com.aiknowledgebase.knowledge.mapper;

import com.aiknowledgebase.knowledge.entity.KnowledgeBase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * KnowledgeBaseMapper 是 knowledge_bases 表的数据访问接口。
 * 它继承 MyBatis-Plus 自带 BaseMapper，在本项目中为 KnowledgeService 提供知识库 CRUD 能力。
 */
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {
}
