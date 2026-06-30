package com.aiknowledgebase.knowledge.dto;

import java.time.LocalDateTime;

/**
 * DocumentSummaryResponse 是文档列表接口的响应 DTO。
 * 它只返回文档元数据而不返回 Markdown 正文，在本项目中保证列表查询轻量。
 *
 * @param id 来自 Document.java 的文档主键。
 * @param knowledgeBaseId 来自 Document.java 的所属知识库 ID。
 * @param title 来自 Document.java 的文档标题。
 * @param summary 来自 Document.java 的文档摘要。
 * @param status 来自 Document.java 的文档状态。
 * @param createdAt 来自 Document.java 的创建时间。
 * @param updatedAt 来自 Document.java 的更新时间。
 */
public record DocumentSummaryResponse(
        Long id,
        Long knowledgeBaseId,
        String title,
        String summary,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
