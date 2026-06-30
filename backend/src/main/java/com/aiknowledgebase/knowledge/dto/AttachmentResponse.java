package com.aiknowledgebase.knowledge.dto;

import java.time.LocalDateTime;

/**
 * AttachmentResponse 是附件列表和上传接口的响应 DTO。
 * 它只暴露前端展示和下载所需字段，在本项目中隐藏服务端真实存储根目录。
 *
 * @param id 来自 Attachment.java 的附件主键。
 * @param documentId 来自 Attachment.java 的文档主键。
 * @param originalFilename 用户上传时的原始文件名。
 * @param contentType 浏览器上传的 MIME 类型。
 * @param sizeBytes 附件大小。
 * @param downloadUrl 当前附件下载接口路径。
 * @param createdAt 附件上传时间。
 */
public record AttachmentResponse(
        Long id,
        Long documentId,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        String downloadUrl,
        LocalDateTime createdAt
) {
}
