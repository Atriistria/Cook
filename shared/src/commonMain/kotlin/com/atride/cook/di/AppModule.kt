package com.atride.cook.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.atride.cook.data.AppDatabase
import com.atride.cook.data.ChatRepository
import com.atride.cook.data.KoogChatRepository
import com.atride.cook.data.dao.MessageDao
import com.atride.cook.data.KtorKoogHttpClientFactory
import com.atride.cook.model.getDatabaseBuilder
import com.atride.cook.ui.screens.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
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
        val client = DeepSeekLLMClient(
            api,
            httpClientFactory = KtorKoogHttpClientFactory()
        )
        val promptExecutor = MultiLLMPromptExecutor(client)
        KoogChatRepository(
            promptExecutor = promptExecutor,
            llmModel = DeepSeekModels.DeepSeekV4Pro,
            systemPrompt = "你是一个专业的 AI 助理。请使用 Markdown 格式友好地回答用户。",
            messageDao = db.messageDao(),
        )
    }

    single { get<AppDatabase>().sessionDao() }
    single { get<AppDatabase>().messageDao() }

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
