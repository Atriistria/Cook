package com.atride.cook.data

import com.atride.cook.model.ChatStreamEvent
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessageStream(message: String): Flow<ChatStreamEvent>
}