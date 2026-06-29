package com.aiknowledgebase;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AiKnowledgeBaseApplication 是后端 Spring Boot 应用入口。
 * 它通过 Spring Boot 自带的启动器加载 Web、Security、MyBatis 等模块，在本项目中负责启动整个 Java 后端服务。
 */
@SpringBootApplication
@MapperScan({"com.aiknowledgebase.auth.mapper", "com.aiknowledgebase.rbac.mapper"})
public class AiKnowledgeBaseApplication {

    /**
     * main 使用 Java 自带的程序入口方法和 SpringApplication 启动容器。
     * 它把命令行参数交给 Spring Boot，在本项目中完成后端进程初始化。
     *
     * @param args Java 自带 main 方法参数，通常由命令行或 IDE 启动配置传入。
     */
    public static void main(String[] args) {
        SpringApplication.run(AiKnowledgeBaseApplication.class, args);
    }
}
