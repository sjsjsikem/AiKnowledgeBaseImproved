# Project Brief

## 项目定位

`ai-knowledge-base` 是企业级 Java 全栈 AI 知识库教程项目。它同时承担两个目标：

- 作为可运行的企业级单体 Web 应用。
- 作为能解释开发流程、架构取舍和关键代码的学习材料。

## 学习对象

适合以下学习者：

- 掌握 Java、SQL、JavaScript/TypeScript 基础。
- 写过简单 Spring Boot 或 React 示例。
- 缺少企业级项目从 PRD 到部署的完整经验。

## 核心痛点

本项目专门解决两个问题：

- 设计和 PRD 只存在聊天上下文中，后续开发容易因为上下文过长或记忆丢失而偏离。
- 项目要么只有整体讲解、没有重点代码注释；要么只有代码注释、缺少整体开发流程讲解。

## 解决策略

- `docs/` 是项目的长期规范中心。
- `adr/` 记录关键架构决策。
- `docs/PLAN.md` 保存项目蓝图、阶段顺序和长期技术范围。
- `docs/README.md` 保存当前项目状态、目录结构、启动方式和接口入口。
- `docs/08-stage-checklists.md` 保存阶段门禁、阶段进度和验收同步要求。
- 代码只在重难点处写教学注释。
- `docs/07-ai-collaboration-rules.md` 约束后续 AI 协作。

## 技术路线

- 后端：Spring Boot 3、Java 17、Spring Security、MyBatis-Plus、Flyway、Redis、Springdoc。
- 前端：React、TypeScript、Vite、React Router、TanStack Query、Zustand、Axios。
- 数据库：MySQL 8。
- AI：OpenAI 兼容 Provider + Mock Provider。
- 架构：单体优先，模块边界清晰。
