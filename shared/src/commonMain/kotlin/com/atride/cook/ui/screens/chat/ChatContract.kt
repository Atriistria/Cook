package com.atride.cook.ui.screens.chat

import com.atride.cook.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null
)

sealed interface ChatIntent {
    data class SendMessage(val text: String) : ChatIntent
}

sealed interface ChatEffect {
    data class ShowToast(val message: String) : ChatEffect
    object ScrollToBottom : ChatEffect
}