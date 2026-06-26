# ADR 0003: Use OpenAI-Compatible Provider

## Status

Accepted

## Context

AI 服务供应商会变化。业务代码不应绑定某一个厂商 SDK。

## Decision

使用 OpenAI 兼容接口 Provider，并提供 Mock Provider。真实模型调用和本地教学演示通过同一业务接口接入。

## Consequences

- 可切换 OpenAI、DeepSeek、通义等兼容服务。
- 无 Key 时仍能演示 AI 业务流程。
- 后续可以扩展限流、重试、成本统计和模型路由。
