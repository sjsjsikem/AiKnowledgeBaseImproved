# AI Collaboration Rules

## 每次继续开发前必须读取

1. `docs/PLAN.md`
2. `docs/README.md`
3. `docs/00-project-brief.md`
4. `docs/01-prd.md`
5. `docs/02-architecture.md`
6. `docs/03-development-standard.md`
7. `docs/04-database-design.md`
8. `docs/05-api-contract.md`
9. `docs/08-stage-checklists.md`
10. 当前阶段验收文档，例如 `docs/stage-{n}-acceptance.md`
11. `docs/adr/` 下所有已接受的 ADR

## 不允许做的事

- 不允许未经 ADR 引入微服务、网关、注册中心。
- 不允许绕过 Flyway 修改数据库。
- 不允许新增业务但不更新文档。
- 不允许 Entity 直接暴露给前端。
- 不允许把普通 CRUD 写成逐行翻译式注释；但业务源码必须按 `03-development-standard.md` 的教程注释规范，为类、方法、关键参数和业务变量说明项目职责和链路作用。
- 不允许读取、更新或重建已移除的 `docs/06-learning-guide.md`；学习引导不是 Agent 开发阶段的规范输入。

## 上下文恢复问题

当上下文过长或记忆丢失时，先回答：

1. 当前阶段是什么？
2. 当前阶段的验收清单完成到哪里？
3. 本次修改涉及哪些 API、表、页面？
4. 是否需要更新 PRD、API 契约、数据库设计、阶段清单或阶段验收文档？
5. 已经运行了哪些测试？
