# API Contract

## 基础路径

所有接口使用 `/api` 作为后端 context path。

## 统一响应

成功：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败：

```json
{
  "code": 40000,
  "message": "请求参数错误",
  "data": null
}
```

## 分页响应

```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "pageSize": 10
}
```

## 认证

登录成功后返回：

```json
{
  "accessToken": "...",
  "user": {}
}
```

后续请求头：

```text
Authorization: Bearer <accessToken>
```

## API 路径规划

- `/api/auth/**`
- `/api/users/me`
- `/api/admin/**`
- `/api/knowledge-bases/**`
- `/api/documents/**`
- `/api/attachments/**`
- `/api/files/**`
- `/api/ai/**`
- `/api/rag/**`
- `/api/system/info`

## Stage 1 认证接口

### 注册

```text
POST /api/auth/register
```

请求：

```json
{
  "username": "alice",
  "password": "Password123",
  "nickname": "Alice",
  "email": "alice@example.com"
}
```

响应 `data`：

```json
{
  "accessToken": "...",
  "user": {
    "id": 1,
    "username": "alice",
    "nickname": "Alice",
    "email": "alice@example.com",
    "status": "ENABLED",
    "roles": ["USER"],
    "permissions": []
  }
}
```

### 登录

```text
POST /api/auth/login
```

请求：

```json
{
  "username": "alice",
  "password": "Password123"
}
```

### 当前用户

```text
GET /api/users/me
Authorization: Bearer <accessToken>
```

### 退出登录

```text
POST /api/auth/logout
Authorization: Bearer <accessToken>
```

当前 Stage 1 退出登录由前端清除 Token 完成；服务端 Token 黑名单将在 Stage 5 Redis 阶段实现。

## Stage 2 RBAC 管理接口

后台接口均需要 JWT，并由 Spring Security 根据权限编码控制访问。

### 后台用户列表

```text
GET /api/admin/users?page=1&pageSize=10
Authorization: Bearer <accessToken>
```

需要权限：`admin:user:read`

响应 `data`：

```json
{
  "items": [
    {
      "id": 1,
      "username": "admin",
      "nickname": "系统管理员",
      "email": "admin@example.com",
      "status": "ENABLED",
      "roles": ["ADMIN", "USER"],
      "permissions": ["admin:user:read", "admin:user:write"]
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 10
}
```

### 用户启停

```text
PATCH /api/admin/users/{userId}/status
```

需要权限：`admin:user:write`

请求：

```json
{
  "status": "DISABLED"
}
```

### 用户角色分配

```text
PUT /api/admin/users/{userId}/roles
```

需要权限：`admin:user:write`

请求：

```json
{
  "roleIds": [1, 2]
}
```

### 角色列表

```text
GET /api/admin/roles
```

需要权限：`admin:role:read`

### 创建角色

```text
POST /api/admin/roles
```

需要权限：`admin:role:write`

请求：

```json
{
  "code": "EDITOR",
  "name": "编辑员",
  "description": "负责知识库内容编辑"
}
```

### 角色权限分配

```text
PUT /api/admin/roles/{roleId}/permissions
```

需要权限：`admin:role:write`

请求：

```json
{
  "permissionIds": [1, 2]
}
```

### 权限列表

```text
GET /api/admin/permissions
```

需要权限：`admin:role:read`

## Stage 3 知识库与文档接口

知识库与文档接口均需要 JWT。Stage 3 的安全边界是“当前用户只能访问自己创建的知识库及其文档”，由 `KnowledgeService.java` 根据 `SecurityUtils.currentUser()` 和 `knowledge_bases.owner_id` 校验。

### 知识库列表

```text
GET /api/knowledge-bases
Authorization: Bearer <accessToken>
```

响应 `data`：

```json
[
  {
    "id": 1,
    "name": "Java 后端学习",
    "description": "Spring Boot 和企业级工程实践",
    "visibility": "PRIVATE",
    "documentCount": 2,
    "createdAt": "2026-06-30T10:00:00",
    "updatedAt": "2026-06-30T10:00:00"
  }
]
```

### 创建知识库

```text
POST /api/knowledge-bases
```

请求：

```json
{
  "name": "Java 后端学习",
  "description": "Spring Boot 和企业级工程实践"
}
```

### 更新知识库

```text
PUT /api/knowledge-bases/{knowledgeBaseId}
```

请求：

```json
{
  "name": "Java 全栈学习",
  "description": "后端、前端、AI 和 RAG"
}
```

### 删除知识库

```text
DELETE /api/knowledge-bases/{knowledgeBaseId}
```

删除知识库时会逻辑删除该知识库下的文档元数据。

### 文档列表

```text
GET /api/knowledge-bases/{knowledgeBaseId}/documents
```

响应 `data`：

```json
[
  {
    "id": 1,
    "knowledgeBaseId": 1,
    "title": "统一响应设计",
    "summary": "ApiResponse 和错误码约定",
    "status": "DRAFT",
    "createdAt": "2026-06-30T10:10:00",
    "updatedAt": "2026-06-30T10:20:00"
  }
]
```

### 创建文档

```text
POST /api/knowledge-bases/{knowledgeBaseId}/documents
```

请求：

```json
{
  "title": "统一响应设计",
  "summary": "ApiResponse 和错误码约定",
  "content": "# 统一响应\n\n后端统一返回 code/message/data。"
}
```

### 文档详情

```text
GET /api/documents/{documentId}
```

响应 `data`：

```json
{
  "id": 1,
  "knowledgeBaseId": 1,
  "title": "统一响应设计",
  "summary": "ApiResponse 和错误码约定",
  "status": "DRAFT",
  "content": "# 统一响应\n\n后端统一返回 code/message/data。",
  "createdAt": "2026-06-30T10:10:00",
  "updatedAt": "2026-06-30T10:20:00"
}
```

### 保存文档

```text
PUT /api/documents/{documentId}
```

请求：

```json
{
  "title": "统一响应设计",
  "summary": "ApiResponse、ErrorCode 和前端解包",
  "status": "PUBLISHED",
  "content": "# 统一响应\n\n后端统一返回 code/message/data。"
}
```

### 删除文档

```text
DELETE /api/documents/{documentId}
```

## Stage 4 附件与版本历史接口

附件与版本接口均需要 JWT。它们不单独使用管理员权限，而是复用文档所属知识库的所有权校验。

### 文档版本列表

```text
GET /api/documents/{documentId}/versions
```

响应 `data`：

```json
[
  {
    "id": 1,
    "documentId": 1,
    "versionNo": 2,
    "title": "统一响应设计",
    "summary": "ApiResponse、ErrorCode 和前端解包",
    "status": "PUBLISHED",
    "content": "# 统一响应\n\n后端统一返回 code/message/data。",
    "createdBy": 1,
    "createdAt": "2026-06-30T16:00:00"
  }
]
```

### 回滚文档版本

```text
POST /api/documents/{documentId}/versions/{versionId}/rollback
```

响应 `data` 为回滚后的文档详情。回滚成功后会新增一条版本快照，用于记录这次回滚操作。

### 删除文档版本

```text
DELETE /api/documents/{documentId}/versions/{versionId}
```

删除前会校验文档所有权和版本归属。该接口用于清理过旧或不再需要的历史快照。

### 附件列表

```text
GET /api/documents/{documentId}/attachments
```

响应 `data`：

```json
[
  {
    "id": 1,
    "documentId": 1,
    "originalFilename": "design-notes.pdf",
    "contentType": "application/pdf",
    "sizeBytes": 10240,
    "downloadUrl": "/api/attachments/1/download",
    "createdAt": "2026-06-30T16:10:00"
  }
]
```

### 上传附件

```text
POST /api/documents/{documentId}/attachments
Content-Type: multipart/form-data
```

请求字段：

```text
file=<binary>
```

### 下载附件

```text
GET /api/attachments/{attachmentId}/download
```

该接口返回文件流，不使用统一 JSON 响应。前端通过 Axios 携带 `Authorization` 并以 Blob 接收。

### 删除附件

```text
DELETE /api/attachments/{attachmentId}
```

## Trace ID

后端响应头返回 `X-Trace-Id`。前端遇到接口错误时应保留该值，方便排查。
