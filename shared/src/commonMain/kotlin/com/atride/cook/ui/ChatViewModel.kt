package com.atride.cook.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atride.cook.data.ChatRepository
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.ChatStreamEvent
import com.atride.cook.model.Session
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingText: String = "",
    val streamingReasoning: String = "",
)

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    private val _streamingText = MutableStateFlow("")
    private val _streamingReasoning = MutableStateFlow("")
    private var aiGenerateJob: Job? = null

    val sessionsFlow: StateFlow<List<Session>> = repository.getSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChatUiState(sessionId: String): StateFlow<ChatUiState> {
        return combine(
            repository.getMessagesFlow(sessionId),
            _isGenerating,
            _streamingText,
            _streamingReasoning,
        ) { messages, isGenerating, streamingText, streamingReasoning ->
            ChatUiState(
                messages = if (isGenerating) messages.filterNot { it.isGenerating } else messages,
                isGenerating = isGenerating,
                streamingText = streamingText,
                streamingReasoning = streamingReasoning,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatUiState()
        )
    }

    fun sendMessage(sessionId: String, text: String) {
        if (text.isEmpty() || _isGenerating.value) return

        _isGenerating.value = true

        aiGenerateJob = viewModelScope.launch {
            try {
                repository.sendMessage(sessionId, text).collect { event ->
                    when (event) {
                        is ChatStreamEvent.Text -> {
                            _streamingText.value += event.text
                        }
                        is ChatStreamEvent.Think -> {
                            _streamingReasoning.value += event.text
                        }
                        is ChatStreamEvent.TokenUsage -> {}
                    }
                }
            } finally {
                _isGenerating.value = false
                _streamingText.value = ""
                _streamingReasoning.value = ""
            }
        }
    }

    fun stopGenerating() {
        aiGenerateJob?.cancel()
        aiGenerateJob = null
        _isGenerating.value = false
        _streamingText.value = ""
        _streamingReasoning.value = ""
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }
}