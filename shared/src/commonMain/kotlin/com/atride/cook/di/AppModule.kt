package com.atride.cook.di

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.params.additionalPropertiesOf
import ai.koog.prompt.processor.ResponseProcessor
import ai.koog.serialization.JSONSerializer
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.atride.cook.data.AppDatabase
import com.atride.cook.data.ChatRepository
import com.atride.cook.data.KoogChatRepository
import com.atride.cook.data.KtorKoogHttpClientFactory
import com.atride.cook.model.getDatabaseBuilder
import com.atride.cook.ui.screens.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
        val agentConfig = AIAgentConfig(
            prompt = prompt(
                id = "companion-assistant",
                params = LLMParams(
                    additionalProperties = mapOf(
                        "thinking" to buildJsonObject {
                            put("type", "enabled")
                        },
                        "reasoning_effort" to JsonPrimitive("high")
                    )
                )
            ) {
                system("你是一个专业的 AI 助理。请使用 Markdown 格式友好地回答用户。")
            },
            model = DeepSeekModels.DeepSeekV4Pro,
            maxAgentIterations = 10
        )

        val agent = AIAgent(
            promptExecutor = MultiLLMPromptExecutor(
                DeepSeekLLMClient(
                    "",
                    httpClientFactory = KtorKoogHttpClientFactory()
                )
            ),
            agentConfig = agentConfig
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
