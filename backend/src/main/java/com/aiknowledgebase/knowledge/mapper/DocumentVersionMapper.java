package com.aiknowledgebase.knowledge.mapper;

import com.aiknowledgebase.knowledge.entity.DocumentVersion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * DocumentVersionMapper 是 document_versions 表的数据访问接口。
 * 它继承 MyBatis-Plus 自带 BaseMapper，在本项目中为版本快照、版本列表和回滚提供数据库读写能力。
 */
public interface DocumentVersionMapper extends BaseMapper<DocumentVersion> {
}
