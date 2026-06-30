package com.aiknowledgebase.knowledge.dto;

import java.time.LocalDateTime;

/**
 * DocumentVersionResponse 是文档版本历史接口的响应 DTO。
 * 它返回历史快照的元数据和正文，在本项目中让前端可以查看并回滚到某个版本。
 *
 * @param id 来自 DocumentVersion.java 的版本主键。
 * @param documentId 来自 DocumentVersion.java 的文档主键。
 * @param versionNo 来自 DocumentVersion.java 的递增版本号。
 * @param title 该版本保存时的文档标题。
 * @param summary 该版本保存时的文档摘要。
 * @param status 该版本保存时的文档状态。
 * @param content 该版本保存时的 Markdown 正文。
 * @param createdBy 生成该版本的用户 ID。
 * @param createdAt 该版本生成时间。
 */
public record DocumentVersionResponse(
        Long id,
        Long documentId,
        Integer versionNo,
        String title,
        String summary,
        String status,
        String content,
        Long createdBy,
        LocalDateTime createdAt
) {
}
