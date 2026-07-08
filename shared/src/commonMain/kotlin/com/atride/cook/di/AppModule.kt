package com.atride.cook.di

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.atride.cook.data.AppDatabase
import com.atride.cook.data.ChatRepository
import com.atride.cook.data.KoogChatRepository
import com.atride.cook.data.KtorKoogHttpClientFactory
import com.atride.cook.model.getDatabaseBuilder
import com.atride.cook.ui.screens.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {

    single<AppDatabase> {
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single { get<AppDatabase>().sessionDao() }

    single<ChatRepository> {
        val agent = AIAgent(
            promptExecutor = MultiLLMPromptExecutor(DeepSeekLLMClient("", httpClientFactory = KtorKoogHttpClientFactory())),
            llmModel = DeepSeekModels.DeepSeekV4Pro
        )
        KoogChatRepository(agent)
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
