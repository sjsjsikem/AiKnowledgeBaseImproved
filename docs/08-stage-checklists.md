# Stage Checklists

## 通用阶段门禁

- [ ] 每阶段开始前先读 `PLAN.md`、`00-project-brief.md`、`02-architecture.md`、`03-development-standard.md`。
- [ ] 每阶段新增或修改业务源码时，必须按 `03-development-standard.md` 的教程型注释规范补齐类、组件、业务方法和业务入参说明。
- [ ] 每阶段结束必须更新 API、数据库、学习路线和验收文档。
- [ ] 每阶段结束必须运行对应测试。

## Stage 0：规范与工程骨架

- [x] 上传并保留 `PLAN.md`。
- [x] 创建 README。
- [x] 创建 docs 规范体系。
- [x] 创建 ADR 目录和基础决策。
- [x] 创建 Spring Boot 后端基础工程。
- [x] 创建 React 前端基础工程。
- [x] 创建 Docker Compose。
- [x] 创建本地启动和停止脚本。
- [x] 后端 `mvn test` 通过。
- [x] 前端 `npm run build` 通过。

## Stage 1：认证与当前用户

- [x] users 表。
- [x] 注册接口。
- [x] 登录接口。
- [x] JWT 生成与解析。
- [x] 当前用户接口。
- [x] 前端登录页。
- [x] 前端注册页。
- [x] 路由守卫。

## Stage 2：RBAC 与管理员基础

- [x] roles、permissions、user_roles、role_permissions 表。
- [x] 权限加载。
- [x] 管理员用户管理。
- [x] 管理员角色管理。
- [x] 管理员权限管理。

## Stage 3：知识库与文档

- [x] knowledge_bases 表。
- [x] documents 和 document_contents 表。
- [x] 知识库 CRUD。
- [x] 文档 CRUD。
- [x] Markdown 编辑页面。

## Stage 4：附件与版本历史

- [x] attachments 表。
- [x] document_versions 表。
- [x] 附件上传。
- [x] 版本快照。
- [x] 版本回滚。
- [x] 版本删除。

## Stage 5：缓存与性能

- [ ] Redis 封装。
- [ ] 热门知识库缓存。
- [ ] 文档详情缓存。
- [ ] 缓存失效策略。

## Stage 6：AI 基础能力

- [ ] OpenAI 兼容 Provider。
- [ ] Mock Provider。
- [ ] 摘要。
- [ ] 单文档问答。
- [ ] 标题建议。
- [ ] 标签建议。

## Stage 7：RAG 与流式输出

- [ ] document_chunks 表。
- [ ] 文档切片。
- [ ] 知识库检索。
- [ ] 引用片段。
- [ ] SSE 流式输出。

## Stage 8：部署与收尾

- [ ] 后端 Dockerfile。
- [ ] 前端 Dockerfile。
- [ ] Nginx 配置。
- [ ] 演示数据。
- [ ] 项目复盘文档。
