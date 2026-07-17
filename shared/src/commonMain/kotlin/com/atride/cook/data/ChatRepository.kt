package com.atride.cook.data

import com.atride.cook.model.ChatStreamEvent
import com.atride.cook.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessageStream(message: String, sessionId: String): Flow<ChatStreamEvent>
    suspend fun getMessages(sessionId: String): List<ChatMessage>
    suspend fun saveMessages(sessionId: String, messages: List<ChatMessage>)
}
