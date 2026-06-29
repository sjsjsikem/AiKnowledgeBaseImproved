package com.aiknowledgebase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig 是后端 Web MVC 通用配置类。
 * 它实现 Spring MVC 自带的 WebMvcConfigurer，在本项目中配置前端开发服务器访问后端 API 的跨域规则。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * addCorsMappings 注册跨域访问规则。
     * 它使用 Spring MVC 自带的 CorsRegistry 允许 Vite 开发端口访问后端，在本项目中支撑前后端分离联调。
     *
     * @param registry Spring MVC 自带的跨域注册对象，用于声明路径、来源、方法和响应头。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:5174",
                        "http://127.0.0.1:5173",
                        "http://127.0.0.1:5174"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Trace-Id")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
