package com.aiknowledgebase.knowledge.dto;

import java.time.LocalDateTime;

/**
 * DocumentDetailResponse 是文档详情接口的响应 DTO。
 * 它把 documents 元数据和 document_contents 正文合并返回，在本项目中为 Markdown 编辑器提供完整编辑数据。
 *
 * @param id 来自 Document.java 的文档主键。
 * @param knowledgeBaseId 来自 Document.java 的所属知识库 ID。
 * @param title 来自 Document.java 的文档标题。
 * @param summary 来自 Document.java 的文档摘要。
 * @param status 来自 Document.java 的文档状态。
 * @param content 来自 DocumentContent.java 的 Markdown 正文。
 * @param createdAt 来自 Document.java 的创建时间。
 * @param updatedAt 来自 Document.java 的更新时间。
 */
public record DocumentDetailResponse(
        Long id,
        Long knowledgeBaseId,
        String title,
        String summary,
        String status,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
