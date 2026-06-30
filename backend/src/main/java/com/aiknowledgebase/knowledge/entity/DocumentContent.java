package com.aiknowledgebase.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DocumentContent 是 document_contents 表对应的 MyBatis-Plus 实体。
 * 它把 Markdown 正文从 documents 元数据表中拆出来，在本项目中避免列表接口读取大字段。
 */
@Getter
@Setter
@TableName("document_contents")
public class DocumentContent {

    // id 使用 MyBatis-Plus 自带 @TableId 和数据库自增策略，作为 document_contents 表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // documentId 来自 Document.java 的主键，用于建立一篇文档和一份正文的一对一关系。
    private Long documentId;

    // content 保存 Markdown 正文，由文档编辑器提交，在本项目中供详情、AI 和 RAG 阶段继续使用。
    private String content;

    // createdAt 记录正文创建时间，用于后续版本历史和审计扩展。
    private LocalDateTime createdAt;

    // updatedAt 记录正文更新时间，用于判断文档正文最近一次保存时间。
    private LocalDateTime updatedAt;
}
