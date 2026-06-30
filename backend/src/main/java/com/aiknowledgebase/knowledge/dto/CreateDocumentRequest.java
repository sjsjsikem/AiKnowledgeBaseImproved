package com.aiknowledgebase.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreateDocumentRequest 是创建文档接口的请求 DTO。
 * 它把文档元数据和 Markdown 正文组合成一个前端提交对象，在本项目中由 Service 拆分写入 documents 和 document_contents。
 *
 * @param title 前端传入的文档标题，用于 documents.title。
 * @param summary 前端传入的文档摘要，用于 documents.summary。
 * @param content 前端传入的 Markdown 正文，用于 document_contents.content。
 */
public record CreateDocumentRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 512)
        String summary,

        @NotBlank
        String content
) {
}
