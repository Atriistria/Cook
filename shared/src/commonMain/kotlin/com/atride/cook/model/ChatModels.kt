package com.atride.cook.model

import kotlin.time.Clock

data class Session(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: String,
    val messageCount: Int = 0
)

enum class MessageRole {
    User, Assistant
}

data class ChatMessage(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

object MockData {
    val sessions = listOf(
        Session("s1", "Kotlin协程原理探讨", "协程的挂起函数本质上是编译器层面的状态机转换...", "10:32", 12),
        Session("s2", "项目架构方案设计", "NiA风格的状态管理用UiState data class加普通函数调用就很清晰", "09:15", 8),
        Session("s3", "写一首关于秋天的诗", "秋风起兮白云飞，草木黄落兮雁南归", "昨天", 6),
        Session("s4", "DeepSeek模型对比", "DeepSeek-R1有671B参数，采用MoE架构...", "昨天", 15),
        Session("s5", "旅行计划：京都", "建议游玩3-4天，第一天去清水寺和二年坂...", "周一", 22),
        Session("s6", "Voyager vs Navigation", "JetBrains的Navigation Compose现在已经是官方推荐方案了", "周一", 4),
    )

    val currentMessages = listOf(
        ChatMessage("m1", "帮我看看这段Kotlin代码有什么问题：\n```kotlin\nfun fetchData() {\n    val result = api.getData()\n    textView.text = result\n}\n```", MessageRole.User),
        ChatMessage("m2", """这段代码有几个问题：

**1. 主线程阻塞**
`api.getData()` 是网络请求，在 Android 上不允许在主线程执行。需要用协程包装：

```kotlin
fun fetchData() = viewModelScope.launch {
    val result = withContext(Dispatchers.IO) {
        api.getData()
    }
    textView.text = result
}
```

**2. 缺少错误处理**
网络请求可能失败，建议加 try-catch。

**3. View 直接持有**
如果是 Fragment/Activity，直接用没问题。但在 ViewModel 里不要持有 View 引用。""", MessageRole.Assistant),
        ChatMessage("m3", "那协程的Dispatchers.Default和Dispatchers.IO有什么区别？什么时候该用哪个？", MessageRole.User),
        ChatMessage("m4", """好问题。两者的核心区别在于**线程池的设计目标**不同：

| 特性 | Dispatchers.Default | Dispatchers.IO |
|------|-------------------|----------------|
| 线程数 | CPU核心数（最少2） | 最多64个 |
| 适用场景 | CPU密集型任务 | IO密集型任务 |
| 典型操作 | 计算、排序、解析 | 网络请求、文件读写、数据库 |

**经验法则**
- 计算/排序/解析 → `Default`
- 网络/文件/DB → `IO`

不过 Ktor 等库内部已经做了线程切换，你不需要手动指定 `IO`。""", MessageRole.Assistant),
    )
}
