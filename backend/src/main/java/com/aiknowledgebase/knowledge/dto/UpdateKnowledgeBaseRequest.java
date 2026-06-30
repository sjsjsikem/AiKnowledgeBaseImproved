package com.aiknowledgebase.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * UpdateKnowledgeBaseRequest 是更新知识库接口的请求 DTO。
 * 它使用 Spring Validation 自带注解校验可编辑字段，在本项目中避免前端直接修改 ownerId 等安全字段。
 *
 * @param name 前端传入的知识库名称，用于更新 knowledge_bases.name。
 * @param description 前端传入的知识库说明，用于更新 knowledge_bases.description。
 */
public record UpdateKnowledgeBaseRequest(
        @NotBlank
        @Size(max = 128)
        String name,

        @Size(max = 512)
        String description
) {
}
