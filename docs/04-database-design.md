# Database Design

## 数据库约定

- 数据库名：`ai_knowledge_base`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_0900_ai_ci`
- 迁移工具：Flyway

## Migration 规则

- 目录：`backend/src/main/resources/db/migration`
- 命名：`V{版本号}__{说明}.sql`
- migration 提交后不得修改历史文件。
- 所有结构变更必须新增 migration。

## 表规划

Stage 1-8 将逐步创建：

- `users`
- `roles`
- `permissions`
- `user_roles`
- `role_permissions`
- `knowledge_bases`
- `documents`
- `document_contents`
- `document_versions`
- `attachments`
- `ai_request_logs`
- `document_chunks`
- `audit_logs`

## 通用字段

核心业务表：

- `id BIGINT PRIMARY KEY`
- `created_at DATETIME`
- `updated_at DATETIME`
- `deleted TINYINT`

关系表：

- `id BIGINT PRIMARY KEY`
- 关联 ID 字段
- `created_at DATETIME`

## Stage 0 基础表

Stage 0 只创建 `app_metadata`，用于确认 Flyway 已正常运行。业务表从 Stage 1 开始按阶段加入。

## Stage 1 users 表

`users` 用于保存系统登录账号。

| 字段 | 说明 |
| --- | --- |
| `id` | 用户 ID |
| `username` | 登录用户名，唯一 |
| `password_hash` | BCrypt 密码哈希 |
| `nickname` | 昵称 |
| `email` | 邮箱 |
| `status` | 用户状态，当前支持 `ENABLED` |
| `deleted` | 逻辑删除标记 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

当前 Stage 1 尚未创建 RBAC 关系表，注册用户默认返回 `USER` 角色用于前端展示；真实角色和权限将在 Stage 2 接入。
