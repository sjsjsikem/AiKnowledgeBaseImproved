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

## Trace ID

后端响应头返回 `X-Trace-Id`。前端遇到接口错误时应保留该值，方便排查。
