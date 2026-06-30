# Stage 5 Acceptance

## 范围

Stage 5 完成 Redis 缓存与性能优化：

- Redis 缓存封装，业务层不直接散落 `StringRedisTemplate` 调用。
- 当前用户知识库列表缓存，作为常用入口缓存示例。
- 文档详情缓存，作为大字段详情缓存示例。
- 退出登录 Token 黑名单，演示 Redis 在认证链路中的短 TTL 状态存储。
- 写操作后的缓存失效策略，保证 MySQL 仍是唯一事实来源。

## 验收步骤

1. 启动 MySQL 和 Redis。
2. 启动后端，确认应用可以连接 Redis。
3. 登录前端并创建知识库。
4. 首次访问知识库列表，确认后端可返回数据。
5. 再次访问知识库列表，确认接口仍返回一致数据。
6. 新建或删除文档后，确认知识库列表中的文档数量会更新。
7. 打开文档详情，修改并保存文档，确认再次打开时返回最新标题、摘要、状态和正文。
8. 执行退出登录后，继续使用旧 Token 请求受保护接口，确认返回未登录。

## 验证命令

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

结果：

- `mvn test`：通过。
- `npm run build`：通过。

## 运行时抽查

使用临时后端端口 `18080`、本地 MySQL 和 Redis 完成抽查：

- 注册测试用户并创建知识库、文档。
- 连续读取知识库列表后，Redis 中生成 `kb:hot:user:{userId}`。
- 读取文档详情后，Redis 中生成 `doc:detail:user:{userId}:doc:{documentId}`。
- 保存文档后再次读取详情，返回更新后的标题和发布状态。
- 调用退出登录后，继续使用旧 Token 请求 `/api/users/me` 返回 401。

## 学习重点

- Cache Aside 的读取流程是“先查缓存，未命中再查数据库并回填缓存”。
- 写操作完成后必须删除或刷新相关缓存，否则前端会看到旧数据。
- Redis 缓存只能保存可重建数据，不能替代 MySQL 的事实来源。
- Token 黑名单适合用 Redis TTL 保存，因为它只需要活到 JWT 原本过期时间。
