package com.aiknowledgebase.knowledge.dto;

import java.time.LocalDateTime;

/**
 * KnowledgeBaseResponse 是知识库列表和详情接口的响应 DTO。
 * 它只暴露前端需要展示的知识库字段，在本项目中隐藏 ownerId、deleted 等内部字段。
 *
 * @param id 来自 KnowledgeBase.java 的知识库主键。
 * @param name 来自 KnowledgeBase.java 的知识库名称。
 * @param description 来自 KnowledgeBase.java 的知识库说明。
 * @param visibility 来自 KnowledgeBase.java 的可见性字段。
 * @param documentCount 由 KnowledgeService 统计的知识库文档数量。
 * @param createdAt 来自 KnowledgeBase.java 的创建时间。
 * @param updatedAt 来自 KnowledgeBase.java 的更新时间。
 */
public record KnowledgeBaseResponse(
        Long id,
        String name,
        String description,
        String visibility,
        Long documentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
