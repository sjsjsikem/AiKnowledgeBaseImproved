package com.aiknowledgebase.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * UpdateDocumentRequest 是更新文档接口的请求 DTO。
 * 它承载编辑器保存时提交的标题、摘要、状态和正文，在本项目中由 Service 分别更新元数据表和正文表。
 *
 * @param title 前端传入的文档标题，用于更新 documents.title。
 * @param summary 前端传入的文档摘要，用于更新 documents.summary。
 * @param status 前端传入的文档状态，Stage 3 支持 DRAFT 和 PUBLISHED。
 * @param content 前端传入的 Markdown 正文，用于更新 document_contents.content。
 */
public record UpdateDocumentRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 512)
        String summary,

        @NotBlank
        @Size(max = 32)
        String status,

        @NotBlank
        String content
) {
}
