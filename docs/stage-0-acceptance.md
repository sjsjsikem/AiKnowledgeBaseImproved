# Stage 0 Acceptance

## 验收时间

2026-06-26

## 已完成

- 项目蓝图保留在 `docs/PLAN.md`。
- README 和 docs 规范体系已创建。
- 后端 Spring Boot 工程可编译并通过测试。
- 前端 React 工程可安装依赖并完成生产构建。
- Docker Compose、启动脚本和停止脚本已创建。

## 已执行命令

```powershell
cd backend
mvn test
```

结果：通过。

```powershell
cd frontend
npm install
npm run build
```

结果：通过。

## 下阶段入口

Stage 1 实现认证与当前用户：

- `users` 表。
- 注册接口。
- 登录接口。
- JWT 生成与解析。
- 当前用户接口。
- 前端登录、注册、路由守卫。
