package com.atride.cook.common

import cook.shared.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

object SystemPromptLoader {

    @OptIn(ExperimentalResourceApi::class)
    val value: String by lazy {
        runBlocking(Dispatchers.IO) {
            Res.readBytes("files/cook-system-prompt.md").decodeToString()
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun load(): String {
        return withContext(Dispatchers.IO) {
            Res.readBytes("files/cook-system-prompt.md").decodeToString()
        }
    }
}
