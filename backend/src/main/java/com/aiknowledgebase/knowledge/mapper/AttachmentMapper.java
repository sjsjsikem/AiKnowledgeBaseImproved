package com.aiknowledgebase.knowledge.mapper;

import com.aiknowledgebase.knowledge.entity.Attachment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * AttachmentMapper 是 attachments 表的数据访问接口。
 * 它继承 MyBatis-Plus 自带 BaseMapper，在本项目中为附件上传、列表、下载和删除提供元数据读写能力。
 */
public interface AttachmentMapper extends BaseMapper<Attachment> {
}
