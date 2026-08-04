package com.atride.cook.data

import com.atride.cook.data.local.ChatLocalDataSource
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.ChatStreamEvent
import com.atride.cook.model.MessageRole
import com.atride.cook.model.Session
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatRepositoryImpl(
    private val localDataSource: ChatLocalDataSource,
    private val aiService: KoogService
) : ChatRepository {

    override fun getSessionsFlow(): Flow<List<Session>> {
        return localDataSource.getSessionsFlow()
    }

    override fun getMessagesFlow(sessionId: String): Flow<List<ChatMessage>> {
        return localDataSource.getMessagesFlow(sessionId)
    }

    override suspend fun deleteSession(sessionId: String) {
        localDataSource.deleteSession(sessionId)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun sendMessage(sessionId: String, userMessage: String): Flow<ChatStreamEvent> = channelFlow {

        localDataSource.ensureSessionExists(sessionId)

        val userMsg = ChatMessage(
            id = Uuid.random().toString(),
            content = userMessage,
            role = MessageRole.USER
        )
        localDataSource.saveMessage(sessionId, userMsg)

        val history = localDataSource.getMessages(sessionId)

        val aiMsgId = Uuid.random().toString()
        val emptyAiMsg = ChatMessage(
            id = aiMsgId,
            content = "",
            role = MessageRole.ASSISTANT,
            isGenerating = true
        )
        localDataSource.saveMessage(sessionId, emptyAiMsg)

        var fullText = ""
        var fullReasoning = ""
        var dbWriteJob: Job? = null

        // 🌟 你的神来之笔：防抖写入机制，保护数据库和 UI 性能
        suspend fun scheduleDbUpdate() {
            dbWriteJob?.cancel()
            dbWriteJob = launch {
                delay(100.milliseconds)
                localDataSource.updateMessageContent(aiMsgId, fullText, fullReasoning)
            }
        }

        aiService.streamChat(
            message = userMessage,
            history = history.filter { it.id != aiMsgId },
            sessionId = sessionId
        ).collect { event ->
            send(event)

            when (event) {
                is ChatStreamEvent.Text -> {
                    fullText += event.text
                    scheduleDbUpdate()
                }
                is ChatStreamEvent.Think -> {
                    fullReasoning += event.text // 注意这里是 event.text，确保和类定义匹配
                    scheduleDbUpdate()
                }
                is ChatStreamEvent.TokenUsage -> {
                    dbWriteJob?.cancel() // 取消可能正在等待的防抖任务
                    // 确保最后一帧数据立刻落库
                    localDataSource.updateMessageContent(aiMsgId, fullText, fullReasoning)

                    val finalMsg = localDataSource.getMessages(sessionId).find { it.id == aiMsgId }
                    finalMsg?.let {
                        localDataSource.saveMessage(sessionId, it.copy(isGenerating = false))
                    }
                }
                else -> {}
            }
        }
    }
}