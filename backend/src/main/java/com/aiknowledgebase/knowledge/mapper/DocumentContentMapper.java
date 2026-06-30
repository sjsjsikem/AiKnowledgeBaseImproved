package com.aiknowledgebase.knowledge.mapper;

import com.aiknowledgebase.knowledge.entity.DocumentContent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * DocumentContentMapper 是 document_contents 表的数据访问接口。
 * 它继承 MyBatis-Plus 自带 BaseMapper，在本项目中负责 Markdown 正文的独立读写。
 */
public interface DocumentContentMapper extends BaseMapper<DocumentContent> {
}
