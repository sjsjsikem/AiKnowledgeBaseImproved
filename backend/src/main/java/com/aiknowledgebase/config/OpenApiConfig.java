package com.aiknowledgebase.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig 是 Swagger/OpenAPI 文档配置类。
 * 它使用 Spring 自带的 @Configuration 和 springdoc-openapi 的 OpenAPI 对象，在本项目中生成后端接口文档入口。
 */
@Configuration
public class OpenApiConfig {

    /**
     * aiKnowledgeBaseOpenApi 创建 OpenAPI 元信息对象。
     * 它通过 springdoc-openapi 的 OpenAPI 和 Info 类设置标题、版本和描述，在本项目中帮助学习者查看接口契约。
     *
     * @return OpenAPI 来自 springdoc-openapi，用于生成 Swagger UI 展示内容。
     */
    @Bean
    public OpenAPI aiKnowledgeBaseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ai-knowledge-base API")
                        .version("0.0.1")
                        .description("Enterprise Java full-stack AI knowledge base tutorial API"));
    }
}
