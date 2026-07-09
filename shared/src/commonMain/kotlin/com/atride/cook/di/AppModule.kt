package com.atride.cook.di

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStreaming
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResultsStreaming
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls
import ai.koog.agents.ext.agent.chatAgentStrategy
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.streaming.StreamFrame
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.atride.cook.data.AppDatabase
import com.atride.cook.data.ChatRepository
import com.atride.cook.data.KoogChatRepository
import com.atride.cook.data.KtorKoogHttpClientFactory
import com.atride.cook.model.getDatabaseBuilder
import com.atride.cook.ui.screens.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import ai.koog.agents.core.agent.GraphAIAgent.FeatureContext
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStreaming
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResultsStreaming
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openai.OpenAIResponsesParams
import ai.koog.prompt.executor.clients.openai.base.models.ReasoningEffort
import ai.koog.prompt.executor.clients.openai.models.ReasoningConfig
import ai.koog.prompt.executor.clients.openai.models.ReasoningSummary
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.streaming.toMessageResponse
import kotlinx.coroutines.flow.toList

val agentEventFlow = MutableSharedFlow<StreamFrame>(extraBufferCapacity = 100)
const val api = ""

val appModule = module {

    single<AppDatabase> {
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single { get<AppDatabase>().sessionDao() }

    single<ChatRepository> {
        val prompt = prompt(
            id = "companion-assistant",
            params = LLMParams(
                additionalProperties = mapOf(
                    "thinking" to buildJsonObject {
                        put("type", "enabled")
                    },
                    "reasoning_effort" to JsonPrimitive("high"),
                    "stream" to JsonPrimitive("true")
                )
            )
        ) {
            system("你是一个专业的 AI 助理。请使用 Markdown 格式友好地回答用户。")
        }

        val client = DeepSeekLLMClient(
            api,
            httpClientFactory = KtorKoogHttpClientFactory()
        )

        val agent = AIAgent.builder()
            .graphStrategy(streamingWithToolsStrategy())
            .promptExecutor(MultiLLMPromptExecutor(client))
            .prompt(prompt)
            .maxIterations(5)
            .llmModel(DeepSeekModels.DeepSeekV4Pro)
            .install {
                handleEvents {
                    onLLMStreamingFrameReceived { ctx ->
                        agentEventFlow.tryEmit(ctx.streamFrame)
                    }
                }
            }
            .build()

        KoogChatRepository(agent, agentEventFlow)
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

fun streamingWithToolsStrategy() = strategy("streaming_loop") {
    val nodeCallLLM by nodeLLMRequestStreaming().transform {
        it.toList().toMessageResponse()
    }
    val nodeSendToolResults by nodeLLMSendToolResultsStreaming().transform {
        it.toList().toMessageResponse()
    }
    val executeTools by nodeExecuteTools(parallel = true)

    edge(nodeStart forwardTo nodeCallLLM)
    edge(nodeCallLLM forwardTo executeTools onToolCalls { true })
    edge(executeTools forwardTo nodeSendToolResults)
    edge(nodeSendToolResults forwardTo executeTools onToolCalls { true })
    edge(nodeSendToolResults forwardTo nodeFinish onTextMessage { true })
    edge(nodeCallLLM forwardTo nodeFinish onTextMessage { true })
}
