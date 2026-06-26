# ai-knowledge-base

企业级 Java 全栈 AI 知识库教程项目。项目面向有一定基础、但缺少企业级项目开发经验的软件开发新手，目标是用一个可运行、可讲解、可持续扩展的工程，系统学习认证、RBAC、知识库、文档、附件、版本、缓存、AI、RAG、流式输出和部署。

## 当前实施阶段

当前仓库处于 **Stage 1：认证与当前用户**。

已实现目标：

- `docs/` 规范体系与 ADR 架构决策记录。
- Spring Boot 3 + Java 17 后端基础工程。
- React + TypeScript + Vite 前端基础工程。
- MySQL + Redis 本地 Docker Compose。
- 后端统一响应、错误码、业务异常、全局异常、Trace ID、请求日志、健康检查、OpenAPI。
- 前端 API 客户端、响应解包、登录态 store、路由结构、登录/注册/知识库/文档/后台阶段入口。
- 用户注册、登录、JWT 认证、当前用户和退出登录。

Stage 2 将实现 RBAC 与管理员基础。

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
├── docs/                    # PRD、架构、规范、学习路线、阶段清单和 ADR
├── frontend/                # React 前端工程
├── scripts/                 # 本地启动/停止脚本
├── PLAN.md                  # 用户确认后的项目蓝图
└── README.md
```

## 必读文档

继续开发前按顺序阅读：

1. `PLAN.md`
2. `docs/00-project-brief.md`
3. `docs/01-prd.md`
4. `docs/02-architecture.md`
5. `docs/03-development-standard.md`
6. `docs/07-ai-collaboration-rules.md`
7. `docs/08-stage-checklists.md`

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
```

前端开发服务器已将 `/api` 代理到 `http://localhost:8080`。

## 开发纪律

- 先更新文档和契约，再写代码。
- 所有数据库结构变更必须通过 Flyway migration。
- Entity 不直接暴露给前端。
- Controller 不写复杂业务逻辑。
- 复杂代码写教学注释，普通 CRUD 不写无意义注释。
- 每个阶段结束必须更新 `docs/08-stage-checklists.md` 和 `docs/06-learning-guide.md`。
