package com.atride.cook.data

import com.atride.cook.model.ChatStreamEvent
import com.atride.cook.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class DeepSeekRepository(
    private val httpClient: KtorKoogHttpClient,
    private val systemPrompt: String
) : ChatRepository {

    override suspend fun getMessages(sessionId: String): List<ChatMessage> = emptyList()

    override suspend fun saveMessages(sessionId: String, messages: List<ChatMessage>) {
        // 备份实现，不持久化
    }

    override fun sendMessageStream(message: String, sessionId: String): Flow<ChatStreamEvent> {
        // 1. 构建请求体 JSON
        val body = buildJsonObject {
            put("model", "deepseek-v4-pro")
            put("stream", true)
            putJsonObject("thinking") { put("type", "enabled") }
            put("reasoning_effort", "high")
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "system")
                    put("content", systemPrompt)
                }
                addJsonObject {
                    put("role", "user")
                    put("content", message)
                }
            }
        }.toString()

        return httpClient.sse(
            path = "/chat/completions",
            requestBody = body,
            requestBodyType = String::class,
            dataFilter = { it != "[DONE]" },
            decodeStreamingResponse = { it },
            processStreamingChunk = { raw -> parseChunk(raw) }
        )
    }

    private fun parseChunk(raw: String): ChatStreamEvent? {
        val root = Json.parseToJsonElement(raw).jsonObject
        val choice = root["choices"]?.jsonArray?.firstOrNull()?.jsonObject
        val delta = choice?.get("delta")?.jsonObject

        // 1. reasoning_content → Think
        val reasoning = delta?.get("reasoning_content")?.jsonPrimitive?.contentOrNull
        if (!reasoning.isNullOrBlank()) return ChatStreamEvent.Think(reasoning)

        // 2. content → Text
        val content = delta?.get("content")?.jsonPrimitive?.contentOrNull
        if (!content.isNullOrBlank()) return ChatStreamEvent.Text(content)

        // 3. usage → TokenUsage（最后一帧，delta 为空但 usage 有值）
        val usage = root["usage"]?.jsonObject
        if (usage != null) {
            return ChatStreamEvent.TokenUsage(
                inputTokens = usage["prompt_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                outputTokens = usage["completion_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                totalTokens = usage["total_tokens"]?.jsonPrimitive?.intOrNull ?: 0
            )
        }

        return null
    }
}
