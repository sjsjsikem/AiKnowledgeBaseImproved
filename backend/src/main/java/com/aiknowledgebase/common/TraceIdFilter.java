package com.aiknowledgebase.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceIdFilter 是请求链路追踪过滤器。
 * 它继承 Spring Web 自带的 OncePerRequestFilter，在本项目中为每次请求生成或透传 X-Trace-Id，方便前端报错和后端日志对齐。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    // TRACE_ID_HEADER 是前后端约定的请求追踪响应头名称。
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    // MDC_KEY 是 SLF4J MDC 日志上下文中的键名，用于把 traceId 写入当前线程日志。
    private static final String MDC_KEY = "traceId";

    /**
     * doFilterInternal 为当前请求准备 traceId 并清理日志上下文。
     * 它使用 HttpServletRequest 读取请求头、HttpServletResponse 写回响应头，在本项目中支撑问题排查闭环。
     *
     * @param request Java Servlet 自带的 HttpServletRequest，表示当前 HTTP 请求。
     * @param response Java Servlet 自带的 HttpServletResponse，表示当前 HTTP 响应。
     * @param filterChain Java Servlet 自带的过滤器链，用于继续执行后续处理。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // 教学重点：MDC 会把 traceId 放进当前请求线程的日志上下文；配合响应头，前端报错和后端日志能对上同一次请求。
        MDC.put(MDC_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Web 容器线程会复用，请求结束必须清理 MDC，避免下一个请求误用上一个请求的 traceId。
            MDC.remove(MDC_KEY);
        }
    }
}
