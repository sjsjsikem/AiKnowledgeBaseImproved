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

## Stage 2 RBAC 表

Stage 2 创建 `roles`、`permissions`、`user_roles`、`role_permissions` 四张表，用两层多对多关系表达权限：

```text
users -> user_roles -> roles -> role_permissions -> permissions
```

### roles

| 字段 | 说明 |
| --- | --- |
| `id` | 角色 ID |
| `code` | 角色编码，唯一，例如 `ADMIN`、`USER` |
| `name` | 角色名称 |
| `description` | 角色说明 |
| `status` | 角色状态，当前支持 `ENABLED` |
| `deleted` | 逻辑删除标记 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

### permissions

| 字段 | 说明 |
| --- | --- |
| `id` | 权限 ID |
| `code` | 权限编码，唯一，例如 `admin:user:read` |
| `name` | 权限名称 |
| `description` | 权限说明 |
| `deleted` | 逻辑删除标记 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

### user_roles

| 字段 | 说明 |
| --- | --- |
| `id` | 关系 ID |
| `user_id` | 用户 ID |
| `role_id` | 角色 ID |
| `created_at` | 创建时间 |

### role_permissions

| 字段 | 说明 |
| --- | --- |
| `id` | 关系 ID |
| `role_id` | 角色 ID |
| `permission_id` | 权限 ID |
| `created_at` | 创建时间 |

Stage 2 初始化：

- `ADMIN`、`USER` 两个角色。
- `admin:user:read`、`admin:user:write`、`admin:role:read`、`admin:role:write` 四个后台权限。
- 本地演示账号 `admin / Admin123456`。
- 所有已有用户补充 `USER` 角色。

## Stage 3 知识库与文档表

Stage 3 创建 `knowledge_bases`、`documents`、`document_contents` 三张表，用“知识库容器 -> 文档元数据 -> 文档正文”的结构表达内容管理关系：

```text
users -> knowledge_bases -> documents -> document_contents
```

设计重点：

- `knowledge_bases.owner_id` 绑定当前用户，是知识库和文档访问控制的根。
- `documents` 只保存标题、摘要、状态等元数据，列表接口不读取 Markdown 大字段。
- `document_contents` 保存 Markdown 正文，与 `documents` 一对一。
- Stage 3 先做个人知识库所有权；协作共享、附件和版本历史后续阶段扩展。

### knowledge_bases

| 字段 | 说明 |
| --- | --- |
| `id` | 知识库 ID |
| `owner_id` | 所属用户 ID |
| `name` | 知识库名称 |
| `description` | 知识库说明 |
| `visibility` | 可见性，Stage 3 默认为 `PRIVATE` |
| `deleted` | 逻辑删除标记 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

### documents

| 字段 | 说明 |
| --- | --- |
| `id` | 文档 ID |
| `knowledge_base_id` | 所属知识库 ID |
| `title` | 文档标题 |
| `summary` | 文档摘要 |
| `status` | 文档状态，当前支持 `DRAFT`、`PUBLISHED` |
| `deleted` | 逻辑删除标记 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

### document_contents

| 字段 | 说明 |
| --- | --- |
| `id` | 正文 ID |
| `document_id` | 文档 ID，唯一 |
| `content` | Markdown 正文 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |
