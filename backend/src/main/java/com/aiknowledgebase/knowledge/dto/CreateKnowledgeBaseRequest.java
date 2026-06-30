package com.aiknowledgebase.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreateKnowledgeBaseRequest 是创建知识库接口的请求 DTO。
 * 它使用 Spring Validation 自带注解校验名称和说明，在本项目中作为知识库创建表单的输入边界。
 *
 * @param name 前端传入的知识库名称，用于 knowledge_bases.name。
 * @param description 前端传入的知识库说明，用于解释知识库业务范围。
 */
public record CreateKnowledgeBaseRequest(
        @NotBlank
        @Size(max = 128)
        String name,

        @Size(max = 512)
        String description
) {
}
