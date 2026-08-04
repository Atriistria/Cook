package com.atride.cook.data.local

import com.atride.cook.model.ChatMessage
import com.atride.cook.model.Session
import kotlinx.coroutines.flow.Flow

interface ChatLocalDataSource {

    fun getMessagesFlow(sessionId: String): Flow<List<ChatMessage>>

    suspend fun getMessages(sessionId: String): List<ChatMessage>

    suspend fun saveMessage(sessionId: String, message: ChatMessage)

    suspend fun updateMessageContent(messageId: String, content: String, reasoning: String? = null)

    suspend fun ensureSessionExists(sessionId: String, defaultTitle: String = "新对话")

    /**
     * 补充 1：获取侧边栏的会话列表 (支持实时响应新会话或最后一条消息的变化)
     */
    fun getSessionsFlow(): Flow<List<Session>>

    /**
     * 补充 2：更新会话标题 (AI 往往会根据前两句对话，自动生成一个精简的标题)
     */
    suspend fun updateSessionTitle(sessionId: String, title: String)

    suspend fun deleteSession(sessionId: String)

    suspend fun clearMessages(sessionId: String)
}