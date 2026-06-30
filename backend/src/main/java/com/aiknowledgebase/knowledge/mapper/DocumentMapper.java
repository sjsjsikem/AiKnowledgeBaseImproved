package com.aiknowledgebase.knowledge.mapper;

import com.aiknowledgebase.knowledge.entity.Document;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * DocumentMapper 是 documents 表的数据访问接口。
 * 它继承 MyBatis-Plus 自带 BaseMapper，在本项目中为文档列表、详情和逻辑删除提供元数据读写能力。
 */
public interface DocumentMapper extends BaseMapper<Document> {
}
