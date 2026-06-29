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

## Trace ID

后端响应头返回 `X-Trace-Id`。前端遇到接口错误时应保留该值，方便排查。
