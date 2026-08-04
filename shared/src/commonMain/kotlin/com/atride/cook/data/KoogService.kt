package com.atride.cook.data

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStreaming
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResultsStreaming
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.streaming.StreamFrame
import ai.koog.prompt.streaming.toMessageResponse
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.ChatStreamEvent
import com.atride.cook.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.toList

class KoogService(
    private val promptExecutor: PromptExecutor,
    private val llmModel: LLModel,
    private val systemPrompt: String,
    private val toolRegistry: ToolRegistry = ToolRegistry {},
) {

    fun streamChat(
        message: String,
        history: List<ChatMessage>, // 传入干净的 UI 模型列表
        sessionId: String
    ): Flow<ChatStreamEvent> = channelFlow {

        val chatPrompt = prompt("chat") {
            system(systemPrompt)

            history.forEach { msg ->
                when (msg.role) {
                    MessageRole.USER -> user(msg.content)
                    MessageRole.ASSISTANT -> assistant(msg.content)
                    MessageRole.SYSTEM -> {}
                }
            }
        }

        val agent = AIAgent.builder()
            .graphStrategy(streamingWithToolsStrategy())
            .promptExecutor(promptExecutor)
            .prompt(chatPrompt)
            .toolRegistry(toolRegistry)
            .llmModel(llmModel)
            .install {
                handleEvents {
                    onLLMStreamingFrameReceived { ctx ->
                        when (val frame = ctx.streamFrame) {
                            is StreamFrame.TextDelta ->
                                trySend(ChatStreamEvent.Text(frame.text))

                            is StreamFrame.ReasoningDelta ->
                                frame.text?.let { trySend(ChatStreamEvent.Think(it)) }

                            is StreamFrame.End -> frame.metaInfo.let { meta ->
                                trySend(
                                    ChatStreamEvent.TokenUsage(
                                        inputTokens = meta.inputTokensCount ?: 0,
                                        outputTokens = meta.outputTokensCount ?: 0,
                                        totalTokens = meta.totalTokensCount ?: 0
                                    )
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
            .build()

        agent.run(message, sessionId)
    }
}

private fun streamingWithToolsStrategy() = strategy("streaming_loop") {
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
