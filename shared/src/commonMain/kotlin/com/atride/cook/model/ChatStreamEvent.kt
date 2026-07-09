package com.atride.cook.model

sealed interface ChatStreamEvent {
    data class Think(val text: String) : ChatStreamEvent

    data class Text(val text: String) : ChatStreamEvent

    data class TokenUsage(
        val inputTokens: Int,
        val outputTokens: Int,
        val totalTokens: Int
    ) : ChatStreamEvent
}