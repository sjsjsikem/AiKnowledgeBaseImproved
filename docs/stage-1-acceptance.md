# Stage 1 Acceptance

## 验收时间

2026-06-26

## 已完成

- 新增 `users` 表 migration：`V2__create_users_table.sql`。
- 实现注册、登录、JWT、当前用户和退出登录接口。
- 实现 JWT 请求过滤器，将 Bearer Token 解析为当前登录用户。
- 前端登录页和注册页接入真实接口。
- 前端业务路由接入登录守卫。
- 前端刷新后通过 `/users/me` 恢复用户资料。

## 已执行命令

```powershell
cd backend
mvn test
```

结果：通过。

```powershell
cd frontend
npm run build
```

结果：通过。

## 接口验收

在后端 `http://localhost:18080/api` 下验证：

- `POST /auth/register`：成功返回 token 和用户资料。
- `GET /users/me`：携带 Bearer Token 成功返回当前用户。
- `POST /auth/logout`：成功返回。
- `POST /auth/login`：成功返回新 token 和用户资料。

## 当前边界

- Stage 1 注册用户默认返回 `USER` 角色。
- 真实 RBAC 表、角色权限加载和管理员后台将在 Stage 2 实现。
- 服务端 Token 黑名单将在 Stage 5 Redis 阶段实现。
