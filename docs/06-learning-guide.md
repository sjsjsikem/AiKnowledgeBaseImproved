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

本项目业务源码会在类、组件、方法、关键参数和业务变量顶部保留教程型注释。阅读时优先看这些注释理解“这个文件在全栈链路中的位置”，再进入方法体看具体实现。

业务变量也属于学习重点，尤其是前端 Hook 状态、`useQuery`/`useMutation` 配置、派生展示数据，以及后端依赖字段、状态常量和关键查询结果。注释不会逐行解释代码，而是解释它连接了哪些对象、调用了哪些 Spring/Java/React/项目内能力，以及它对当前阶段功能闭环的作用。

阅读前端页面时建议按这个顺序看：

1. 组件顶部注释，先判断页面承担的业务职责。
2. `useState`、Zustand selector、`useQuery`、`useMutation` 等业务变量注释，理解页面状态和后端接口如何连接。
3. 事件处理方法注释，理解用户操作如何转换成 API 请求。
4. JSX 结构，最后看数据如何展示。

## 重难点代码索引

| 阶段 | 主题 | 文件 | 学习重点 |
| --- | --- | --- | --- |
| Stage 0 | 应用入口 | `backend/src/main/java/com/aiknowledgebase/AiKnowledgeBaseApplication.java` | Spring Boot 启动入口和 Mapper 扫描 |
| Stage 0 | 统一响应 | `backend/src/main/java/com/aiknowledgebase/common/ApiResponse.java` | 后端统一 code/message/data 契约 |
| Stage 0 | 统一错误码 | `backend/src/main/java/com/aiknowledgebase/common/ErrorCode.java` | 业务错误码和前端错误分支 |
| Stage 0 | 业务异常 | `backend/src/main/java/com/aiknowledgebase/common/BusinessException.java` | Service 主动中断失败业务流程 |
| Stage 0 | 分页响应 | `backend/src/main/java/com/aiknowledgebase/common/PageResponse.java` | 列表接口统一分页结构 |
| Stage 0 | Trace ID | `backend/src/main/java/com/aiknowledgebase/common/TraceIdFilter.java` | 请求级链路标识 |
| Stage 0 | 请求日志 | `backend/src/main/java/com/aiknowledgebase/common/RequestLogFilter.java` | 请求方法、路径、状态码、耗时和 Trace ID |
| Stage 0 | 统一异常 | `backend/src/main/java/com/aiknowledgebase/common/GlobalExceptionHandler.java` | 统一错误响应 |
| Stage 0 | CORS 配置 | `backend/src/main/java/com/aiknowledgebase/config/WebConfig.java` | 前后端分离开发跨域白名单 |
| Stage 0 | OpenAPI 配置 | `backend/src/main/java/com/aiknowledgebase/config/OpenApiConfig.java` | Swagger 接口文档入口 |
| Stage 0 | 系统信息接口 | `backend/src/main/java/com/aiknowledgebase/system/SystemInfoController.java` | 前端验证后端运行阶段和连通性 |
| Stage 0 | API 解包 | `frontend/src/api/client.ts` | Axios 拦截器和响应解包 |
| Stage 0 | 系统信息 API | `frontend/src/api/system.ts` | 前端调用 `/system/info` 的封装 |
| Stage 0 | 路由表 | `frontend/src/routes/router.tsx` | 认证页和业务页路由组织 |
| Stage 0 | 认证布局 | `frontend/src/layouts/AuthLayout.tsx` | 登录/注册共用页面框架 |
| Stage 0 | 阶段提示组件 | `frontend/src/components/StageNotice.tsx` | 未实现阶段的教学边界提示 |
| Stage 0 | 前端入口 | `frontend/src/main.tsx` | React、Router、TanStack Query 全局装配 |
| Stage 0 | 前端类型契约 | `frontend/src/types/api.ts` | 前后端响应、分页和阶段类型定义 |
| Stage 1 | users 表 | `backend/src/main/resources/db/migration/V2__create_users_table.sql` | 用户账号表、BCrypt 哈希和逻辑删除 |
| Stage 1 | 用户实体 | `backend/src/main/java/com/aiknowledgebase/auth/entity/User.java` | users 表和 Java Entity 映射 |
| Stage 1 | 用户 Mapper | `backend/src/main/java/com/aiknowledgebase/auth/mapper/UserMapper.java` | MyBatis-Plus 基础数据访问 |
| Stage 1 | 注册请求 DTO | `backend/src/main/java/com/aiknowledgebase/auth/dto/RegisterRequest.java` | Spring Validation 表单校验 |
| Stage 1 | 登录请求 DTO | `backend/src/main/java/com/aiknowledgebase/auth/dto/LoginRequest.java` | 登录接口入参边界 |
| Stage 1 | 认证响应 DTO | `backend/src/main/java/com/aiknowledgebase/auth/dto/AuthResponse.java` | Token 和用户资料返回结构 |
| Stage 1 | 用户资料 DTO | `backend/src/main/java/com/aiknowledgebase/auth/dto/UserProfile.java` | 不暴露 passwordHash 的安全用户资料 |
| Stage 1 | 认证 Controller | `backend/src/main/java/com/aiknowledgebase/auth/controller/AuthController.java` | 登录、注册、当前用户、退出登录 HTTP 入口 |
| Stage 1 | 用户 Controller | `backend/src/main/java/com/aiknowledgebase/auth/controller/UserController.java` | `/users/me` 登录态恢复接口 |
| Stage 1 | JWT 生成解析 | `backend/src/main/java/com/aiknowledgebase/security/JwtService.java` | Token 签发和验签 |
| Stage 1 | JWT 认证过滤器 | `backend/src/main/java/com/aiknowledgebase/security/JwtAuthenticationFilter.java` | Bearer Token 写入安全上下文 |
| Stage 1 | 当前用户模型 | `backend/src/main/java/com/aiknowledgebase/security/CurrentUser.java` | 写入 Spring Security 上下文的用户身份 |
| Stage 1 | 当前用户工具 | `backend/src/main/java/com/aiknowledgebase/security/SecurityUtils.java` | Service 层统一读取登录用户 |
| Stage 1 | 用户访问服务 | `backend/src/main/java/com/aiknowledgebase/auth/service/UserAccessService.java` | JWT 用户 ID 转换为 CurrentUser |
| Stage 1 | 认证业务 | `backend/src/main/java/com/aiknowledgebase/auth/service/AuthService.java` | BCrypt、注册、登录、当前用户 |
| Stage 1 | 安全配置 | `backend/src/main/java/com/aiknowledgebase/config/SecurityConfig.java` | 匿名接口、认证接口和统一 401/403 响应 |
| Stage 1 | 认证 API | `frontend/src/api/auth.ts` | 登录、注册、当前用户、退出登录接口封装 |
| Stage 1 | 登录态 Store | `frontend/src/store/auth.ts` | Token、用户资料、角色和权限持久化 |
| Stage 1 | 路由守卫 | `frontend/src/routes/ProtectedRoute.tsx` | 未登录跳转和原始路径保存 |
| Stage 1 | 登录页 | `frontend/src/pages/LoginPage.tsx` | 登录表单、错误提示和登录后跳转 |
| Stage 1 | 注册页 | `frontend/src/pages/RegisterPage.tsx` | 注册表单和注册后自动登录 |
| Stage 1 | 登录态恢复 | `frontend/src/layouts/AppLayout.tsx` | 刷新后通过 `/users/me` 恢复用户资料 |
| Stage 2 | RBAC 表关系 | `backend/src/main/resources/db/migration/V3__create_rbac_tables.sql` | 用户、角色、权限两层多对多 |
| Stage 2 | 角色实体 | `backend/src/main/java/com/aiknowledgebase/rbac/entity/Role.java` | roles 表和 Java Entity 映射 |
| Stage 2 | 权限实体 | `backend/src/main/java/com/aiknowledgebase/rbac/entity/Permission.java` | permissions 表和 Java Entity 映射 |
| Stage 2 | 用户角色关系实体 | `backend/src/main/java/com/aiknowledgebase/rbac/entity/UserRole.java` | user_roles 多对多关系 |
| Stage 2 | 角色权限关系实体 | `backend/src/main/java/com/aiknowledgebase/rbac/entity/RolePermission.java` | role_permissions 多对多关系 |
| Stage 2 | 角色 Mapper | `backend/src/main/java/com/aiknowledgebase/rbac/mapper/RoleMapper.java` | 按用户查询启用角色 |
| Stage 2 | 权限 Mapper | `backend/src/main/java/com/aiknowledgebase/rbac/mapper/PermissionMapper.java` | 按用户和角色查询权限 |
| Stage 2 | 关系 Mapper | `backend/src/main/java/com/aiknowledgebase/rbac/mapper/UserRoleMapper.java` | 用户角色关系维护 |
| Stage 2 | 关系 Mapper | `backend/src/main/java/com/aiknowledgebase/rbac/mapper/RolePermissionMapper.java` | 角色权限关系维护 |
| Stage 2 | 后台用户 DTO | `backend/src/main/java/com/aiknowledgebase/rbac/dto/AdminUserResponse.java` | 后台用户展示数据边界 |
| Stage 2 | 角色 DTO | `backend/src/main/java/com/aiknowledgebase/rbac/dto/RoleResponse.java` | 角色及权限编码响应结构 |
| Stage 2 | 权限 DTO | `backend/src/main/java/com/aiknowledgebase/rbac/dto/PermissionResponse.java` | 权限字典响应结构 |
| Stage 2 | 授权请求 DTO | `backend/src/main/java/com/aiknowledgebase/rbac/dto/UpdateUserRolesRequest.java` | 用户角色分配入参 |
| Stage 2 | 授权请求 DTO | `backend/src/main/java/com/aiknowledgebase/rbac/dto/UpdateRolePermissionsRequest.java` | 角色权限分配入参 |
| Stage 2 | 用户状态请求 DTO | `backend/src/main/java/com/aiknowledgebase/rbac/dto/UpdateUserStatusRequest.java` | 用户启停入参 |
| Stage 2 | 创建角色请求 DTO | `backend/src/main/java/com/aiknowledgebase/rbac/dto/CreateRoleRequest.java` | 角色创建入参和校验 |
| Stage 2 | RBAC 业务编排 | `backend/src/main/java/com/aiknowledgebase/rbac/service/RbacService.java` | 权限加载、用户角色、角色权限 |
| Stage 2 | 管理员 Controller | `backend/src/main/java/com/aiknowledgebase/rbac/controller/AdminRbacController.java` | 后台用户、角色、权限 HTTP 入口 |
| Stage 2 | 接口权限控制 | `backend/src/main/java/com/aiknowledgebase/config/SecurityConfig.java` | hasAuthority 与后台接口保护 |
| Stage 2 | MyBatis-Plus 分页 | `backend/src/main/java/com/aiknowledgebase/config/MyBatisPlusConfig.java` | 后台用户分页查询插件配置 |
| Stage 2 | 管理员 API | `frontend/src/api/admin.ts` | 后台用户、角色、权限接口封装 |
| Stage 2 | 管理员后台 | `frontend/src/pages/AdminPage.tsx` | 用户启停、角色分配、权限分配 |
| Stage 2 | 权限菜单 | `frontend/src/layouts/AppLayout.tsx` | 根据 RBAC 权限控制管理员菜单展示 |
| Stage 2 | 前端类型契约 | `frontend/src/types/api.ts` | AdminUser、Role、Permission 和授权请求类型 |
| Stage 3 | 知识库与文档表 | `backend/src/main/resources/db/migration/V4__create_knowledge_document_tables.sql` | 知识库、文档元数据、Markdown 正文分表 |
| Stage 3 | 知识库实体 | `backend/src/main/java/com/aiknowledgebase/knowledge/entity/KnowledgeBase.java` | ownerId 所有权边界和知识库容器映射 |
| Stage 3 | 文档实体 | `backend/src/main/java/com/aiknowledgebase/knowledge/entity/Document.java` | 文档标题、摘要、状态等元数据映射 |
| Stage 3 | 文档正文实体 | `backend/src/main/java/com/aiknowledgebase/knowledge/entity/DocumentContent.java` | Markdown 正文从列表元数据中拆分 |
| Stage 3 | 知识库 Mapper | `backend/src/main/java/com/aiknowledgebase/knowledge/mapper/KnowledgeBaseMapper.java` | MyBatis-Plus 知识库 CRUD 数据访问 |
| Stage 3 | 文档 Mapper | `backend/src/main/java/com/aiknowledgebase/knowledge/mapper/DocumentMapper.java` | documents 表元数据读写 |
| Stage 3 | 文档正文 Mapper | `backend/src/main/java/com/aiknowledgebase/knowledge/mapper/DocumentContentMapper.java` | document_contents 正文读写 |
| Stage 3 | 知识库请求 DTO | `backend/src/main/java/com/aiknowledgebase/knowledge/dto/CreateKnowledgeBaseRequest.java` | 创建知识库表单校验 |
| Stage 3 | 知识库更新 DTO | `backend/src/main/java/com/aiknowledgebase/knowledge/dto/UpdateKnowledgeBaseRequest.java` | 更新知识库可编辑字段边界 |
| Stage 3 | 知识库响应 DTO | `backend/src/main/java/com/aiknowledgebase/knowledge/dto/KnowledgeBaseResponse.java` | 不暴露 ownerId 的知识库响应结构 |
| Stage 3 | 文档创建 DTO | `backend/src/main/java/com/aiknowledgebase/knowledge/dto/CreateDocumentRequest.java` | 标题、摘要、Markdown 正文输入边界 |
| Stage 3 | 文档更新 DTO | `backend/src/main/java/com/aiknowledgebase/knowledge/dto/UpdateDocumentRequest.java` | 保存文档时的标题、摘要、状态和正文 |
| Stage 3 | 文档列表 DTO | `backend/src/main/java/com/aiknowledgebase/knowledge/dto/DocumentSummaryResponse.java` | 不加载正文的轻量列表响应 |
| Stage 3 | 文档详情 DTO | `backend/src/main/java/com/aiknowledgebase/knowledge/dto/DocumentDetailResponse.java` | 元数据与正文合并后的编辑器响应 |
| Stage 3 | 知识库业务编排 | `backend/src/main/java/com/aiknowledgebase/knowledge/service/KnowledgeService.java` | 所有权校验、事务、分表写入和 DTO 转换 |
| Stage 3 | 知识库 Controller | `backend/src/main/java/com/aiknowledgebase/knowledge/controller/KnowledgeController.java` | 知识库和文档 HTTP 接口入口 |
| Stage 3 | Mapper 扫描 | `backend/src/main/java/com/aiknowledgebase/AiKnowledgeBaseApplication.java` | 新增 knowledge.mapper 扫描路径 |
| Stage 3 | 系统阶段标记 | `backend/src/main/java/com/aiknowledgebase/system/SystemInfoController.java` | 返回 stage-3-knowledge-documents |
| Stage 3 | 知识库 API | `frontend/src/api/knowledge.ts` | 知识库和文档接口封装 |
| Stage 3 | 知识库管理页 | `frontend/src/pages/KnowledgeBasesPage.tsx` | 知识库 CRUD、文档列表和缓存刷新 |
| Stage 3 | Markdown 编辑器 | `frontend/src/pages/DocumentEditorPage.tsx` | 文档新建、加载、保存和轻量预览 |
| Stage 3 | 前端类型契约 | `frontend/src/types/api.ts` | KnowledgeBase、DocumentSummary、DocumentDetail 类型 |
| Stage 3 | 业务布局状态 | `frontend/src/layouts/AppLayout.tsx` | 顶部栏阶段标记更新 |
