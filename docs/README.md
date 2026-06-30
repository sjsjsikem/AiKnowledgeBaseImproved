# ai-knowledge-base

企业级 Java 全栈 AI 知识库教程项目。项目面向有一定基础、但缺少企业级项目开发经验的软件开发新手，目标是用一个可运行、可讲解、可持续扩展的工程，系统学习认证、RBAC、知识库、文档、附件、版本、缓存、AI、RAG、流式输出和部署。

## 当前实施阶段

当前仓库处于 **Stage 4：附件与版本历史**。

已实现目标：

- `docs/` 规范体系与 ADR 架构决策记录。
- Spring Boot 3 + Java 17 后端基础工程。
- React + TypeScript + Vite 前端基础工程。
- MySQL + Redis 本地 Docker Compose。
- 后端统一响应、错误码、业务异常、全局异常、Trace ID、请求日志、健康检查、OpenAPI。
- 前端 API 客户端、响应解包、登录态 store、路由结构、登录/注册/知识库/文档/后台阶段入口。
- 用户注册、登录、JWT 认证、当前用户和退出登录。
- RBAC 表结构、管理员种子账号、角色权限加载、后台用户启停、用户角色分配、角色权限分配。
- 知识库 CRUD、文档 CRUD、Markdown 编辑器、文档元数据与正文分表、当前用户所有权校验。
- 附件上传、附件下载、附件删除、文档版本快照、版本历史、版本回滚和版本删除。

Stage 5 将实现 Redis 缓存与性能优化。

## 技术栈

后端：

- Java 17
- Spring Boot 3
- Spring Security
- MyBatis-Plus
- Flyway
- MySQL 8
- Redis
- Springdoc OpenAPI
- Maven

前端：

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- Zustand
- Axios

AI：

- OpenAI 兼容接口 Provider
- Mock Provider

## 目录结构

```text
.
├── backend/                 # Spring Boot 后端工程
├── docker/                  # MySQL、Redis 等本地基础设施
├── docs/                    # 项目蓝图、README、PRD、架构、规范、阶段清单、验收文档和 ADR
│   ├── PLAN.md              # 用户确认后的项目蓝图
│   ├── README.md            # 当前阶段、启动方式、接口入口和开发纪律
│   ├── 00-project-brief.md  # 项目定位和目标能力
│   ├── 01-prd.md            # 产品范围和验收主流程
│   ├── 02-architecture.md   # 架构和模块边界
│   ├── 03-development-standard.md
│   ├── 04-database-design.md
│   ├── 05-api-contract.md
│   ├── 07-ai-collaboration-rules.md
│   ├── 08-stage-checklists.md
│   ├── stage-*-acceptance.md
│   └── adr/
├── frontend/                # React 前端工程
└── scripts/                 # 本地启动/停止脚本
```

## 必读文档

继续开发前按顺序阅读：

1. `docs/PLAN.md`
2. `docs/README.md`
3. `docs/00-project-brief.md`
4. `docs/01-prd.md`
5. `docs/02-architecture.md`
6. `docs/03-development-standard.md`
7. `docs/04-database-design.md`
8. `docs/05-api-contract.md`
9. `docs/07-ai-collaboration-rules.md`
10. `docs/08-stage-checklists.md`
11. 当前阶段验收文档，例如 `docs/stage-{n}-acceptance.md`
12. `docs/adr/` 下所有已接受的 ADR

## 启动基础设施

```powershell
docker compose -f docker/docker-compose.yml up -d
```

MySQL：

- Host: `localhost`
- Port: `3307`
- Database: `ai_knowledge_base`
- Username: `root`
- Password: `root123456`

Redis：

- Host: `localhost`
- Port: `6380`

## 启动后端

```powershell
cd backend
mvn test
mvn spring-boot:run
```

常用地址：

- Health: `http://localhost:8080/api/actuator/health`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- System Info: `http://localhost:8080/api/system/info`

认证接口：

- Register: `POST /api/auth/register`
- Login: `POST /api/auth/login`
- Current User: `GET /api/users/me`
- Logout: `POST /api/auth/logout`

本地演示管理员账号：

- Username: `admin`
- Password: `Admin123456`

管理员接口：

- Users: `GET /api/admin/users`
- Update User Status: `PATCH /api/admin/users/{userId}/status`
- Update User Roles: `PUT /api/admin/users/{userId}/roles`
- Roles: `GET /api/admin/roles`
- Create Role: `POST /api/admin/roles`
- Update Role Permissions: `PUT /api/admin/roles/{roleId}/permissions`
- Permissions: `GET /api/admin/permissions`

知识库与文档接口：

- Knowledge Bases: `GET /api/knowledge-bases`
- Create Knowledge Base: `POST /api/knowledge-bases`
- Update Knowledge Base: `PUT /api/knowledge-bases/{knowledgeBaseId}`
- Delete Knowledge Base: `DELETE /api/knowledge-bases/{knowledgeBaseId}`
- Documents: `GET /api/knowledge-bases/{knowledgeBaseId}/documents`
- Create Document: `POST /api/knowledge-bases/{knowledgeBaseId}/documents`
- Document Detail: `GET /api/documents/{documentId}`
- Save Document: `PUT /api/documents/{documentId}`
- Delete Document: `DELETE /api/documents/{documentId}`
- Document Versions: `GET /api/documents/{documentId}/versions`
- Rollback Version: `POST /api/documents/{documentId}/versions/{versionId}/rollback`
- Delete Version: `DELETE /api/documents/{documentId}/versions/{versionId}`
- Attachments: `GET /api/documents/{documentId}/attachments`
- Upload Attachment: `POST /api/documents/{documentId}/attachments`
- Download Attachment: `GET /api/attachments/{attachmentId}/download`
- Delete Attachment: `DELETE /api/attachments/{attachmentId}`

## 启动前端

```powershell
cd frontend
npm install
npm run build
npm run dev
```

前端地址：

```text
http://localhost:5173
http://127.0.0.1:5173
```

前端开发服务器已将 `/api` 代理到 `http://localhost:8080`。

## 开发纪律

- 先更新文档和契约，再写代码。
- 所有数据库结构变更必须通过 Flyway migration。
- Entity 不直接暴露给前端。
- Controller 不写复杂业务逻辑。
- 复杂代码写教学注释，普通 CRUD 不写无意义注释。
- 每个阶段结束必须更新 `docs/README.md`、`docs/08-stage-checklists.md` 和当前阶段验收文档。
- 涉及接口或表结构时，必须同步更新 `docs/05-api-contract.md` 和 `docs/04-database-design.md`。
- `docs/06-learning-guide.md` 已移除，不再作为 Agent 开发输入；后续不得读取、更新或重建该文件。
