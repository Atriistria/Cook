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
import com.atride.cook.data.dao.MessageDao
import com.atride.cook.data.dao.SessionDao
import com.atride.cook.data.entity.MessageEntity
import com.atride.cook.data.entity.SessionEntity
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.ChatStreamEvent
import com.atride.cook.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.toList
import kotlin.time.Clock

class KoogChatRepository(
    private val promptExecutor: PromptExecutor,
    private val llmModel: LLModel,
    private val systemPrompt: String,
    private val toolRegistry: ToolRegistry = ToolRegistry {},
    private val messageDao: MessageDao,
    private val sessionDao: SessionDao,
) : ChatRepository {

    override suspend fun getMessages(sessionId: String): List<ChatMessage> {
        return messageDao.getBySession(sessionId).map { entity ->
            ChatMessage(
                id = entity.id,
                content = entity.content,
                role = when (entity.role) {
                    "user" -> MessageRole.User
                    else -> MessageRole.Assistant
                },
                timestamp = entity.createdAt,
            )
        }
    }

    override suspend fun saveMessages(sessionId: String, messages: List<ChatMessage>) {
        val maxOrder = messageDao.maxSortOrder(sessionId)
        messages.forEachIndexed { index, msg ->
            messageDao.insertOrUpdate(
                MessageEntity(
                    id = msg.id,
                    sessionId = sessionId,
                    role = when (msg.role) {
                        MessageRole.User -> "user"
                        MessageRole.Assistant -> "assistant"
                    },
                    content = msg.content,
                    createdAt = msg.timestamp,
                    sortOrder = maxOrder + 1 + index,
                )
            )
        }
    }

    override fun sendMessageStream(message: String, sessionId: String): Flow<ChatStreamEvent> =
        channelFlow {
            if (sessionDao.getById(sessionId) == null) {
                val now = Clock.System.now().toEpochMilliseconds()
                sessionDao.upsertSession(SessionEntity(
                    id = sessionId,
                    title = "新对话",
                    createdAt = now,
                    updatedAt = now,
                ))
            }

            val history = messageDao.getBySession(sessionId)
            val chatPrompt = prompt("chat") {
                system(systemPrompt)
                history.forEach { msg ->
                    when (msg.role) {
                        "user" -> user(msg.content)
                        "assistant" -> assistant(msg.content)
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
