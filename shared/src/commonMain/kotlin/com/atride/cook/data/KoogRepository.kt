package com.atride.cook.data

import ai.koog.agents.core.agent.AIAgent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class KoogChatRepository(
    private val agent: AIAgent<String, String>
) : ChatRepository {

    override fun sendMessageStream(message: String): Flow<String> = flow {
        println("发送信息")
        try {
            val result = agent.run(message)
            println(result)
            emit(result)
        } catch (e: Exception) {
            println(e)
        }
    }
}