package com.atride.cook.di

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.atride.cook.common.SystemPromptLoader
import com.atride.cook.data.AppDatabase
import com.atride.cook.data.ChatRepository
import com.atride.cook.data.ChatRepositoryImpl
import com.atride.cook.data.KoogService
import com.atride.cook.data.KtorKoogHttpClientFactory
import com.atride.cook.data.dao.MessageDao
import com.atride.cook.data.dao.SessionDao
import com.atride.cook.data.local.ChatLocalDataSource
import com.atride.cook.data.local.ChatLocalDataSourceImpl
import com.atride.cook.data.tools.WebSearchTool
import com.atride.cook.model.getDatabaseBuilder
import com.atride.cook.ui.ChatViewModel
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

    single<MessageDao> { get<AppDatabase>().messageDao() }
    single<SessionDao> { get<AppDatabase>().sessionDao() }

    single<ChatLocalDataSource> {
        ChatLocalDataSourceImpl(messageDao = get(), sessionDao = get())
    }

    single<ChatRepository> {
        ChatRepositoryImpl(localDataSource = get(), aiService = get())
    }

    single<KoogService> {
        val toolRegistry = ToolRegistry {
            tool(WebSearchTool)
        }
        val client = DeepSeekLLMClient(
            api,
            httpClientFactory = KtorKoogHttpClientFactory()
        )
        val promptExecutor = MultiLLMPromptExecutor(client)
        KoogService(
            promptExecutor = promptExecutor,
            llmModel = DeepSeekModels.DeepSeekV4Pro,
            systemPrompt = SystemPromptLoader.value,
            toolRegistry = toolRegistry,
        )
    }

    factory { ChatViewModel(get()) }

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
