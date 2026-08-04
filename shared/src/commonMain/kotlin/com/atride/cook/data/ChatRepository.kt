package com.atride.cook.data

import com.atride.cook.model.ChatMessage
import com.atride.cook.model.ChatStreamEvent
import com.atride.cook.model.Session
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getSessionsFlow(): Flow<List<Session>>

    fun getMessagesFlow(sessionId: String): Flow<List<ChatMessage>>

    suspend fun deleteSession(sessionId: String)

    fun sendMessage(sessionId: String, userMessage: String): Flow<ChatStreamEvent>
}