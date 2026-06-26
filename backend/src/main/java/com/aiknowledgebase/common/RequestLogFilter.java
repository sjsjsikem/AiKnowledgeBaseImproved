package com.aiknowledgebase.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * RequestLogFilter 是请求日志过滤器。
 * 它继承 Spring Web 自带的 OncePerRequestFilter，在本项目中为每一次 HTTP 请求记录方法、路径、状态码、耗时和 Trace ID。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLogFilter extends OncePerRequestFilter {

    // log 是 SLF4J 提供的日志对象，用于输出请求访问日志。
    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    /**
     * doFilterInternal 包裹一次请求并统计耗时。
     * 它使用 Servlet FilterChain 继续后续过滤器和 Controller 调用，在本项目中把访问日志集中记录到公共层。
     *
     * @param request Java Servlet 自带的 HttpServletRequest，表示当前 HTTP 请求。
     * @param response Java Servlet 自带的 HttpServletResponse，表示当前 HTTP 响应。
     * @param filterChain Java Servlet 自带的过滤器链，用于继续执行后续处理。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("{} {} status={} cost={}ms traceId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    cost,
                    response.getHeader(TraceIdFilter.TRACE_ID_HEADER));
        }
    }
}
