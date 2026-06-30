package com.aiknowledgebase.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Document 是 documents 表对应的 MyBatis-Plus 实体。
 * 它保存文档标题、摘要、状态和所属知识库，在本项目中作为列表查询和权限校验的轻量文档元数据。
 */
@Getter
@Setter
@TableName("documents")
public class Document {

    // id 使用 MyBatis-Plus 自带 @TableId 和数据库自增策略，作为 documents 表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // knowledgeBaseId 来自 KnowledgeBase.java 的主键，用于把文档归属到某个知识库。
    private Long knowledgeBaseId;

    // title 是文档标题，由 Markdown 编辑器表单提交，用于列表、编辑器和后续 AI 标题建议。
    private String title;

    // summary 是文档摘要，Stage 3 由用户手写，Stage 6 会接入 AI 自动摘要。
    private String summary;

    // status 是文档状态，Stage 3 支持 DRAFT/PUBLISHED，用于区分草稿和已发布文档。
    private String status;

    // deleted 使用 MyBatis-Plus 自带逻辑删除注解，避免删除文档时影响后续版本历史教学。
    @TableLogic
    private Integer deleted;

    // createdAt 记录文档创建时间，用于列表排序和审计。
    private LocalDateTime createdAt;

    // updatedAt 记录文档更新时间，用于列表展示最近编辑时间。
    private LocalDateTime updatedAt;
}
