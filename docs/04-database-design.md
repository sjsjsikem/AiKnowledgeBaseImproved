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

## Stage 4 附件与版本历史表

Stage 4 创建 `document_versions`、`attachments` 两张表，用于补齐文档编辑后的可追溯能力和附件管理能力：

```text
documents -> document_versions
documents -> attachments
```

设计重点：

- 每次创建或保存文档后写入一条 `document_versions` 快照。
- 回滚不是删除历史，而是把历史快照恢复为当前文档后再生成一条新版本。
- 版本删除用于清理不再需要的历史快照；删除前仍要校验文档所有权和版本归属。
- `attachments` 只保存文件元数据和相对路径，真实文件存放在后端配置的附件目录。
- 附件下载、删除和版本回滚都先通过文档所属知识库校验当前用户所有权。

### document_versions

| 字段 | 说明 |
| --- | --- |
| `id` | 版本 ID |
| `document_id` | 文档 ID |
| `version_no` | 同一文档下递增的版本号 |
| `title` | 该版本的标题快照 |
| `summary` | 该版本的摘要快照 |
| `status` | 该版本的状态快照 |
| `content` | 该版本的 Markdown 正文快照 |
| `created_by` | 创建该版本的用户 ID |
| `created_at` | 版本创建时间 |

### attachments

| 字段 | 说明 |
| --- | --- |
| `id` | 附件 ID |
| `document_id` | 文档 ID |
| `original_filename` | 用户上传时的原始文件名 |
| `stored_filename` | 后端生成的安全存储文件名 |
| `content_type` | 文件 MIME 类型 |
| `size_bytes` | 文件大小 |
| `storage_path` | 相对附件根目录的存储路径 |
| `deleted` | 逻辑删除标记 |
| `created_at` | 上传时间 |
| `updated_at` | 更新时间 |

## Stage 5 Redis 缓存设计

Stage 5 不新增 MySQL 表，而是在 Redis 中保存可重建的派生数据。Redis 中的数据都不能作为唯一事实来源；缓存丢失、过期或 Redis 短暂不可用时，业务必须回退到 MySQL 查询。

### Redis Key 规划

| Key | Value | TTL | 说明 |
| --- | --- | --- | --- |
| `kb:hot:user:{userId}` | `KnowledgeBaseResponse[]` JSON | 5 分钟 | 当前用户常用入口的知识库列表缓存，包含文档数量。 |
| `doc:detail:user:{userId}:doc:{documentId}` | `DocumentDetailResponse` JSON | 10 分钟 | 当前用户可访问文档的详情缓存，包含 Markdown 正文。 |
| `auth:blacklist:{tokenHash}` | `true` | JWT 剩余有效期 | 退出登录后的 Token 黑名单，阻止旧 Token 继续访问。 |

### 缓存失效规则

- 创建、更新、删除知识库后，删除当前用户的 `kb:hot:user:{userId}`。
- 创建、删除文档后，删除当前用户的知识库列表缓存；删除文档时同时删除该文档详情缓存。
- 保存文档或回滚文档版本后，删除并重建当前用户的文档详情缓存，同时删除知识库列表缓存，保证标题、摘要、状态和更新时间一致。
- Token 退出登录后写入黑名单，TTL 使用 JWT 剩余有效期，避免 Redis 中长期堆积无效键。
