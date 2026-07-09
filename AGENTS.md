
# Agents

本文档描述 Cook 的 AI Agent 架构设计与规范，供开发者和贡献者参考。

## 概述

Cook 使用 [Koog Agents](https://docs.koog.ai) 作为 AI 引擎层，通过统一的 `ChatRepository` 接口屏蔽底层 LLM 的差异，在不同的平台（Android / iOS / Desktop）上提供一致的对话体验。

## Architecture

```
UI Layer (Compose Multiplatform)
       │
       ▼
ChatViewModel
       │
       ▼
ChatRepository (Interface)
       │
       ├── KoogChatRepository    ← Koog Agents SDK 实现
       │
       ▼
KoogHttpClient (Interface)
       │
       └── KtorKoogHttpClient    ← Ktor 实现（SSE / HTTP）
```

### 分层职责

- **ChatViewModel** — 管理 UI 状态，处理用户输入，消费 Repository 流式响应
- **ChatRepository** — 定义对话数据访问契约，与 LLM 提供商解耦
- **KoogChatRepository** — 基于 Koog Agents `AIAgent` 的实现，将输入委托给底层 LLM
- **KoogHttpClient** — 通用 HTTP 客户端接口，支持 GET / POST / SSE / lines 四种模式
- **KtorKoogHttpClient** — 基于 Ktor 的多平台 HTTP 实现

## Key Concepts

### 1. Repository 模式

`ChatRepository` 是核心接口，上层 UI 只依赖它，不关心底层是哪个 LLM。切换模型或 AI 框架时只需要替换实现。

```kotlin
interface ChatRepository {
    fun sendMessageStream(message: String): Flow<String>
}
```

### 2. 流式响应

AI 回复通过 Kotlin `Flow<String>` 逐块下发，UI 层累积渲染，实现实时打字机效果。

### 3. 平台 HTTP 客户端

各平台使用对应的 Ktor 引擎：

| 平台            | Ktor 引擎 |
|---------------|---------|
| Android       | OkHttp  |
| iOS           | Darwin  |
| Desktop (JVM) | CIO     |

## Configuration

| 配置项     | 说明                       |
|---------|--------------------------|
| LLM 提供商 | DeepSeek（通过 Koog SDK 接入） |
| 注入方式    | Koin DI                  |
| 流式协议    | SSE (Server-Sent Events) |

## Future Considerations

- 多 LLM 提供商切换（如 OpenAI / Claude / 本地模型）
- 多 Agent 编排（Planner / Tool-calling）
- 对话历史持久化（本地 + 云端）
- 长短期记忆（Long-term Memory）
- 工具调用（Web search / Code interpreter 等）
