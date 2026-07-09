package com.atride.cook.data

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.streaming.StreamFrame
import com.atride.cook.model.ChatStreamEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class KoogChatRepository(
    private val agent: AIAgent<String, String>,
    private val eventFlow: SharedFlow<StreamFrame>
) : ChatRepository {

    override fun sendMessageStream(message: String): Flow<ChatStreamEvent> = channelFlow {
        println("发送信息: $message")

        val agentJob = launch {
            agent.run(message)
        }

        val collectJob = launch {
            eventFlow.collect { frame ->
                when (frame) {
                    is StreamFrame.ReasoningDelta -> {
                        frame.text?.let { send(ChatStreamEvent.Think(it)) }
                    }

                    is StreamFrame.TextDelta -> {
                        send(ChatStreamEvent.Text(frame.text))
                    }

                    is StreamFrame.End -> {
                        val meta = frame.metaInfo
                        send(
                            ChatStreamEvent.TokenUsage(
                                inputTokens = meta.inputTokensCount ?: 0,
                                outputTokens = meta.outputTokensCount ?: 0,
                                totalTokens = meta.totalTokensCount ?: 0
                            )
                        )
                    }

                    else -> {} // TextComplete、ToolCall 等暂不处理
                }
            }
        }

        // 3. 等 agent 跑完，关掉事件收集
        agentJob.join()
        collectJob.cancel()
    }
}
