# ai-knowledge-base 企业级 Java 全栈教程项目蓝图

## Summary

新项目定位为面向“有基础但缺少企业级项目经验”的 Java 全栈教程项目，采用进阶企业版范围：

- 后端：Spring Boot 3、Java 17、Spring Security、MyBatis-Plus、Flyway、Redis、Springdoc OpenAPI。
- 前端：React、TypeScript、Vite、React Router、TanStack Query、Zustand、Axios。
- 数据库：MySQL 8 + Flyway。
- AI：OpenAI 兼容接口 Provider，可切换 OpenAI、DeepSeek、通义等兼容服务。
- 架构：单体优先，但按企业级模块边界组织，包含认证、RBAC、知识库、文档、版本、附件、缓存、RAG、流式 AI、管理员后台、Docker 部署。

核心改进点：项目内必须把 PRD、规范、架构决策、开发流程、学习路线和 AI 协作上下文固化为文档，避免上下文丢失后开发偏离。

## Key Changes

- 新增 `docs/` 规范体系：
  - `00-project-brief.md`：项目定位、学习对象、技术栈和目标能力。
  - `01-prd.md`：角色、功能范围、非目标、验收路径。
  - `02-architecture.md`：后端分层、前端结构、数据流、AI/RAG 调用链。
  - `03-development-standard.md`：编码、接口、异常、日志、事务、测试、注释规范。
  - `04-database-design.md`：MySQL 表结构、索引、Flyway 迁移规则、初始化数据。
  - `05-api-contract.md`：REST API、统一响应、分页、错误码、认证头。
  - `06-learning-guide.md`：每阶段学习重点、阅读顺序、重难点代码索引。
  - `07-ai-collaboration-rules.md`：AI 继续开发前必须读取的上下文恢复规则。
  - `08-stage-checklists.md`：每阶段开发、测试、文档、验收清单。
  - `adr/`：记录架构决策，例如为何用单体、为何第一版用 MySQL、为何通过 Provider 抽象 AI。

- 后端模块建议：
  - `common`：统一响应、错误码、异常、分页、Trace ID。
  - `config`：安全、MyBatis、Redis、OpenAPI、CORS、配置属性。
  - `security`：JWT、当前用户、权限上下文。
  - `auth`：注册、登录、当前用户。
  - `rbac`：用户、角色、权限、后台管理。
  - `knowledge`：知识库、文档、版本、附件。
  - `ai`：AI Provider、摘要、问答、标题建议、标签建议、流式输出。
  - `rag`：文档切片、检索、引用、知识库问答。
  - `audit`：AI 调用日志、后台操作日志。

- 前端模块建议：
  - `api`：Axios 客户端、接口封装、响应解包。
  - `types`：前后端共享契约类型。
  - `store`：登录态、用户权限、UI 状态。
  - `routes`：路由与权限守卫。
  - `layouts`：登录后主框架、后台框架。
  - `pages`：登录、知识库、文档编辑、AI 面板、管理员后台。
  - `components`：表格、表单、Markdown 编辑器、状态组件、权限组件。

## Development Flow

- 每个阶段固定流程：
  - 先更新 PRD 或设计文档。
  - 再实现后端接口和数据库迁移。
  - 再实现前端页面和接口联调。
  - 再补充测试、手动验收记录和学习讲解。
  - 最后更新 `08-stage-checklists.md` 和 `06-learning-guide.md`。

- 阶段 0：规范与工程骨架
  - 建立 Spring Boot、React、Docker、MySQL、Redis、Flyway、OpenAPI。
  - 完成统一响应、统一异常、日志、Trace ID、健康检查。
  - 产出完整 docs 规范骨架。

- 阶段 1：认证与当前用户
  - 注册、登录、JWT、当前用户、退出登录。
  - 前端完成登录页、注册页、路由守卫、Token 持久化。
  - 重点讲解 BCrypt、JWT 过滤器、Axios 拦截器、登录态恢复。

- 阶段 2：RBAC 与管理员基础
  - 用户、角色、权限、用户角色、角色权限。
  - 管理员后台实现用户启停、角色分配、权限分配。
  - 重点讲解 RBAC 表关系、接口权限、前端权限菜单。

- 阶段 3：知识库与文档
  - 知识库 CRUD、文档 CRUD、Markdown 编辑、逻辑删除。
  - 文档正文与元数据可拆表，避免列表查询读取大字段。
  - 重点讲解所有权校验、DTO/Entity 边界、事务边界。

- 阶段 4：附件与版本历史
  - 附件上传、文件元数据、文档版本快照、版本回滚。
  - 重点讲解文件安全、路径校验、历史版本设计、乐观锁思路。

- 阶段 5：缓存与性能
  - Redis 缓存热门知识库、文档详情、Token 黑名单。
  - 实现 Cache Aside、缓存失效、基础防穿透策略。
  - 重点讲解缓存一致性、TTL、缓存失败降级。

- 阶段 6：AI 基础能力
  - OpenAI 兼容 Provider。
  - 实现摘要、单文档问答、标题建议、标签建议。
  - 支持无 Key 的 Mock Provider，保证教学演示不断链。
  - 重点讲解 Prompt 构造、Provider 抽象、AI 调用日志、失败处理。

- 阶段 7：RAG 与流式输出
  - 文档切片、关键词检索或简单向量扩展预留、引用片段、知识库问答。
  - 实现 SSE 流式输出。
  - 重点讲解 RAG 数据流、引用来源、流式接口、前端消费 SSE。

- 阶段 8：部署与收尾
  - Docker Compose 编排后端、前端、MySQL、Redis。
  - Nginx 反向代理前端和 `/api`。
  - 准备演示数据、完整验收路径、项目复盘文档。

## Public Interfaces

- 统一响应：
  - 成功：`{ "code": 0, "message": "success", "data": ... }`
  - 失败：`{ "code": 非0错误码, "message": "错误说明", "data": null }`

- 认证：
  - 登录返回 `accessToken`。
  - 请求头使用 `Authorization: Bearer <token>`。
  - `/api/auth/register`、`/api/auth/login` 匿名访问，其余业务接口默认需要登录。

- API 路径：
  - `/api/auth/**`
  - `/api/users/me`
  - `/api/admin/**`
  - `/api/knowledge-bases/**`
  - `/api/documents/**`
  - `/api/files/**`
  - `/api/ai/**`
  - `/api/rag/**`

- 数据库迁移：
  - 所有建表和初始化数据通过 `backend/src/main/resources/db/migration/V*.sql` 管理。
  - 禁止直接手工改库后不补迁移脚本。

## Test Plan

- 后端：
  - 单元测试覆盖密码校验、权限判断、文档所有权、Prompt 构造。
  - Controller 测试覆盖参数校验、401、403、统一响应。
  - 集成测试覆盖认证、RBAC、知识库、文档、AI Mock Provider。

- 前端：
  - 测试登录态恢复、路由守卫、表单校验、API 错误提示。
  - 手动验收 Markdown 编辑、附件上传、版本回滚、AI 输出、管理员后台。

- 每阶段完成标准：
  - 页面可演示。
  - Swagger 可验证接口。
  - 数据库迁移可从空库执行。
  - README 和对应 docs 已更新。
  - 重难点代码有教学注释。
  - 阶段清单全部完成。

## Assumptions

- 本次规划以“完整项目蓝图”为交付形态。
- 技术栈固定为 `Spring Boot + React + MySQL + Flyway`。
- AI 接入固定采用 OpenAI 兼容接口，并保留 Mock Provider。
- 教学方式采用“整体流程讲解 + 重难点代码注释”，不做逐行注释。
- 当前 `AiKnowledgeBaseImproved` 目录为空，适合按此蓝图从零创建新版项目。
