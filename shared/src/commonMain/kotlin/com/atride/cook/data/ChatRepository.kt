package com.atride.cook.data

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessageStream(message: String): Flow<String>
}