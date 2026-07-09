package com.atride.cook.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atride.cook.data.ChatRepository
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.ChatStreamEvent
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

data class Message(
    val id: String,
    val sender: MessageRole,
    val text: String,
    val reasoningText: String = "", // 新增：用于单独存储思维链的思考路径
    val inputTokens: Int = 0,       // 新增：用于记录输入消耗的 Token
    val outputTokens: Int = 0,      // 新增：用于记录输出生成的 Token
    val timestamp: Long = 0L
)

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

            val userMsg = Message(id = userMsgId, sender = MessageRole.User, text = text)
            // AI 的初始占位符消息，文字和思维链都默认为空
            val aiPlaceholderMsg = Message(id = aiMsgId, sender = MessageRole.Assistant, text = "")

            _uiState.update { state ->
                state.copy(
                    messages = state.messages + userMsg + aiPlaceholderMsg,
                    isSending = true,
                    error = null
                )
            }

            // 本地状态累加器，用于在协程中逐步拼接流式字符
            var accumulatedReasoning = ""
            var accumulatedText = ""

            repository.sendMessageStream(text)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isSending = false) }
                }
                .collect { event ->
                    _uiState.update { state ->
                        val updatedList = state.messages.map { msg ->
                            if (msg.id == aiMsgId) {
                                when (event) {
                                    is ChatStreamEvent.Think -> {
                                        println("Think:${event.text}")
                                        accumulatedReasoning += event.text
                                        msg.copy(reasoningText = accumulatedReasoning)
                                    }
                                    is ChatStreamEvent.Text -> {
                                        println("Text:${event.text}")
                                        accumulatedText += event.text
                                        msg.copy(text = accumulatedText)
                                    }
                                    is ChatStreamEvent.TokenUsage -> {
                                        println("TokenUsage:${event.totalTokens}")
                                        msg.copy(
                                            inputTokens = event.inputTokens,
                                            outputTokens = event.outputTokens
                                        )
                                    }
                                }
                            } else {
                                msg
                            }
                        }
                        state.copy(messages = updatedList)
                    }

                }

            // 流式传输彻底结束，关闭加载状态
            _uiState.update { it.copy(isSending = false) }
        }
    }
}