# Stage 2 Acceptance

## 范围

Stage 2 完成 RBAC 与管理员基础：

- `roles`、`permissions`、`user_roles`、`role_permissions` 四张表。
- 本地演示管理员账号：`admin / Admin123456`。
- 当前用户资料返回真实角色和权限。
- Spring Security 按权限编码保护 `/admin/**`。
- 管理员后台支持用户启停、用户角色分配、角色创建、角色权限分配和权限查看。

## 验收步骤

1. 启动 MySQL 和 Redis。
2. 启动后端，确认 Flyway 执行 `V3__create_rbac_tables.sql`。
3. 使用 `admin / Admin123456` 登录前端。
4. 确认侧边栏显示“管理员后台”。
5. 进入管理员后台，确认用户、角色、权限数据可加载。
6. 创建一个新角色。
7. 给普通用户分配或取消角色。
8. 给角色勾选或取消权限。
9. 使用普通用户登录，确认侧边栏不显示管理员后台。

## 验证命令

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

## 学习重点

- RBAC 不把权限直接挂到用户，而是通过角色间接授权。
- JWT 只保存最小身份信息，角色和权限每次请求从数据库加载。
- 前端隐藏菜单只是体验优化，真正安全边界必须由后端 Spring Security 控制。
