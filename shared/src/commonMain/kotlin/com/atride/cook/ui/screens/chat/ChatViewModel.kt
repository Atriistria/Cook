package com.atride.cook.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atride.cook.data.ChatRepository
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.MessageRole
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class ChatViewModel(
    private val repository: ChatRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ChatEffect>()
    val effect: Flow<ChatEffect> = _effect.receiveAsFlow()

    fun sendIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.SendMessage -> handleSendMessage(intent.text)
        }
    }

    private fun handleSendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val userMsgId = "user_" + Random.nextLong(Long.MAX_VALUE) // 自定义简单 ID 生成即可
            val aiMsgId = "ai_" + Random.nextLong(Long.MAX_VALUE)

            val userMsg = ChatMessage(id = userMsgId, role = MessageRole.User, content = text)
            val aiPlaceholderMsg = ChatMessage(id = aiMsgId, role = MessageRole.Assistant, content = "")

            _uiState.update { state ->
                state.copy(
                    messages = state.messages + userMsg + aiPlaceholderMsg,
                    isSending = true,
                    error = null
                )
            }

            var accumulatedText = ""
            repository.sendMessageStream(text)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isSending = false) }
                }
                .collect { chunk ->
                    accumulatedText += chunk
                    _uiState.update { state ->
                        val updatedList = state.messages.map { msg ->
                            if (msg.id == aiMsgId) msg.copy(content = accumulatedText) else msg
                        }
                        state.copy(messages = updatedList)
                    }
                    _effect.send(ChatEffect.ScrollToBottom)
                }

            _uiState.update { it.copy(isSending = false) }
        }
    }
}