# Learning Guide

## 学习顺序

1. Stage 0：工程骨架、统一响应、统一异常、Trace ID。
2. Stage 1：BCrypt、JWT、Axios 拦截器、登录态恢复。
3. Stage 2：RBAC 表关系、权限加载、管理员后台。
4. Stage 3：知识库、文档、DTO/Entity 边界、事务。
5. Stage 4：附件安全、版本历史、回滚。
6. Stage 5：Redis、Cache Aside、缓存失效。
7. Stage 6：AI Provider、Prompt、Mock Provider。
8. Stage 7：RAG、引用片段、SSE。
9. Stage 8：Docker、Nginx、演示数据。

## 每阶段阅读方式

1. 先读 PRD 中的目标。
2. 再读 API 契约。
3. 再读数据库 migration。
4. 阅读后端 Controller 和 Service。
5. 阅读前端 API 和页面。
6. 最后运行测试和手动验收。

## 代码注释阅读方式

本项目业务源码会在类、组件、方法和关键参数顶部保留教程型注释。阅读时优先看这些注释理解“这个文件在全栈链路中的位置”，再进入方法体看具体实现。注释不会逐行解释代码，而是解释它连接了哪些对象、调用了哪些 Spring/Java/项目内能力，以及它对当前阶段功能闭环的作用。

## 重难点代码索引

| 阶段 | 主题 | 文件 | 学习重点 |
| --- | --- | --- | --- |
| Stage 0 | Trace ID | `backend/src/main/java/com/aiknowledgebase/common/TraceIdFilter.java` | 请求级链路标识 |
| Stage 0 | 统一异常 | `backend/src/main/java/com/aiknowledgebase/common/GlobalExceptionHandler.java` | 统一错误响应 |
| Stage 0 | API 解包 | `frontend/src/api/client.ts` | Axios 拦截器和响应解包 |
| Stage 1 | JWT 生成解析 | `backend/src/main/java/com/aiknowledgebase/security/JwtService.java` | Token 签发和验签 |
| Stage 1 | JWT 认证过滤器 | `backend/src/main/java/com/aiknowledgebase/security/JwtAuthenticationFilter.java` | Bearer Token 写入安全上下文 |
| Stage 1 | 认证业务 | `backend/src/main/java/com/aiknowledgebase/auth/service/AuthService.java` | BCrypt、注册、登录、当前用户 |
| Stage 1 | 登录态恢复 | `frontend/src/layouts/AppLayout.tsx` | 刷新后通过 `/users/me` 恢复用户资料 |
