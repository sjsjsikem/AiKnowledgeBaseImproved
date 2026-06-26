# Architecture

## 架构风格

项目采用模块化单体架构。单体负责降低学习和部署复杂度，模块化负责保证企业级边界清晰。

```text
React SPA
  -> Axios Client
  -> Spring Boot REST API
  -> Controller
  -> Service
  -> Mapper
  -> MySQL / Redis
  -> AI Provider
```

## 后端模块

- `common`：统一响应、错误码、异常、分页、Trace ID。
- `config`：安全、CORS、OpenAPI、MyBatis、Redis、配置属性。
- `security`：JWT、当前用户、权限上下文。
- `auth`：注册、登录、当前用户、退出登录。
- `rbac`：用户、角色、权限、后台管理。
- `knowledge`：知识库、文档、版本、附件。
- `ai`：Provider、摘要、问答、标题建议、标签建议、流式输出。
- `rag`：文档切片、检索、引用、知识库问答。
- `audit`：AI 调用日志、后台操作日志。

## 前端模块

- `api`：Axios 客户端、接口封装、响应解包。
- `types`：接口契约类型。
- `store`：登录态、权限、UI 状态。
- `routes`：路由与权限守卫。
- `layouts`：认证布局、业务布局、后台布局。
- `pages`：登录、注册、知识库、文档编辑、AI、后台。
- `components`：表格、表单、状态、Markdown 编辑器、权限组件。

## 后端调用链

```text
HTTP 请求
  -> TraceIdFilter
  -> Security Filter
  -> Controller 参数校验
  -> Service 业务规则和事务
  -> Mapper 数据访问
  -> ApiResponse 返回
```

## AI/RAG 调用链

```text
前端 AI 操作
  -> /api/ai 或 /api/rag
  -> 权限与文档访问校验
  -> 读取文档或知识库上下文
  -> Prompt 构造
  -> AI Provider
  -> 保存 AI 请求日志
  -> 返回结果或 SSE 流
```
