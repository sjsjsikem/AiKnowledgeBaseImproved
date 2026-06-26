package com.aiknowledgebase.system;

import com.aiknowledgebase.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * SystemInfoController 是系统信息接口。
 * 它使用 Spring MVC Controller 暴露健康以外的项目运行信息，在本项目中帮助学习者确认当前阶段和后端状态。
 */
@RestController
@RequestMapping("/system")
public class SystemInfoController {

    /**
     * info 返回当前项目阶段和运行时间。
     * 它使用 Java 自带 Map 和 OffsetDateTime 组装轻量响应，在本项目中供前端首页和联调检查调用。
     *
     * @return ApiResponse 包装系统信息 Map，保持统一响应格式。
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.success(Map.of(
                "name", "ai-knowledge-base",
                "stage", "stage-1-authentication",
                "status", "running",
                "time", OffsetDateTime.now().toString()
        ));
    }
}
