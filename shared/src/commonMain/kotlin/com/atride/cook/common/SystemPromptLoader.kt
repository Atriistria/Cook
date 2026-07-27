package com.atride.cook.common

import cook.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object SystemPromptLoader {

    /** 同步懒加载，适合 DI 等同步场景直接使用 */
    @OptIn(ExperimentalResourceApi::class)
    val value: String by lazy {
        runBlocking(Dispatchers.IO) {
            Res.readBytes("files/cook-system-prompt.md").decodeToString()
        }
    }

    /**
     * 协程友好的异步加载，适合 suspend 上下文。
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun load(): String {
        return withContext(Dispatchers.IO) {
            Res.readBytes("files/cook-system-prompt.md").decodeToString()
        }
    }
}
