# Stage 3 Acceptance

## 范围

Stage 3 完成知识库与文档基础能力：

- `knowledge_bases`、`documents`、`document_contents` 三张表。
- 当前用户只能访问自己创建的知识库和文档。
- 知识库支持创建、列表、编辑和删除。
- 文档支持创建、列表、详情、保存和删除。
- 前端提供知识库列表页和 Markdown 文档编辑页。

## 验收步骤

1. 启动 MySQL 和 Redis。
2. 启动后端，确认 Flyway 执行 `V4__create_knowledge_document_tables.sql`。
3. 登录前端。
4. 在知识库页面创建一个知识库。
5. 在该知识库下新建一篇 Markdown 文档。
6. 保存文档后确认页面跳转到 `/documents/{documentId}`。
7. 修改文档标题、摘要、状态和正文并再次保存。
8. 返回知识库页面，确认文档列表和知识库文档数量已刷新。
9. 删除文档和知识库，确认列表同步更新。

## 验证命令

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

## 学习重点

- 知识库是当前用户内容资源的聚合根。
- 文档列表只查询元数据，Markdown 正文放在 `document_contents` 中。
- Controller 只处理 HTTP 参数适配；所有权校验和事务边界放在 Service。
- 前端用 TanStack Query 管理列表、详情和保存后的缓存刷新。
