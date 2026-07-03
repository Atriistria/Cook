package com.atride.cook.data

import ai.koog.agents.core.agent.AIAgent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class KoogChatRepository(
    private val agent: AIAgent<String, String>
) : ChatRepository {

    override fun sendMessageStream(message: String): Flow<String> = flow {
        val result = agent.run(message)
        emit(result)
    }
}