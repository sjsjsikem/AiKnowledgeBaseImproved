# ADR 0002: Use MySQL And Flyway

## Status

Accepted

## Context

MySQL 是 Java 企业项目中常见选择。教程项目必须能从空库稳定重建数据库结构，不能依赖手工改库。

## Decision

使用 MySQL 8 作为主数据库，使用 Flyway 管理数据库迁移。

## Consequences

- 学习门槛较低。
- 数据库结构变更有历史记录。
- RAG 向量能力通过接口预留，不在第一阶段绑定向量数据库。
