package com.atride.cook.di

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import com.atride.cook.data.ChatRepository
import com.atride.cook.data.KoogChatRepository
import com.atride.cook.data.KtorKoogHttpClientFactory
import com.atride.cook.ui.screens.chat.ChatViewModel
import org.koin.dsl.module

val appModule = module {
    single<ChatRepository> {
        val agent = AIAgent(
            promptExecutor = MultiLLMPromptExecutor(DeepSeekLLMClient("", httpClientFactory = KtorKoogHttpClientFactory())),
            llmModel = DeepSeekModels.DeepSeekV4Pro
        )
        KoogChatRepository(agent)
    }

    factory { ChatViewModel(get()) }
}
