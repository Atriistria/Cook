package com.atride.cook.di

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.atride.cook.common.SystemPromptLoader
import com.atride.cook.data.AppDatabase
import com.atride.cook.data.ChatRepository
import com.atride.cook.data.KoogChatRepository
import com.atride.cook.data.KtorKoogHttpClientFactory
import com.atride.cook.data.tools.WebSearchTool
import com.atride.cook.model.getDatabaseBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

const val api = ""

val appModule = module {

    single<AppDatabase> {
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single<ChatRepository> {
        val db = get<AppDatabase>()

        val toolRegistry = ToolRegistry {
            tool(WebSearchTool)
        }

        val client = DeepSeekLLMClient(
            api,
            httpClientFactory = KtorKoogHttpClientFactory()
        )
        val promptExecutor = MultiLLMPromptExecutor(client)
        KoogChatRepository(
            promptExecutor = promptExecutor,
            llmModel = DeepSeekModels.DeepSeekV4Pro,
            systemPrompt = SystemPromptLoader.value,
            toolRegistry = toolRegistry,
            messageDao = db.messageDao(),
            sessionDao = db.sessionDao(),
        )
    }

    single { get<AppDatabase>().sessionDao() }

}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule)
    }
}

fun initKoinHelper() {
    initKoin()
}
