package com.atride.cook.data.local

import com.atride.cook.data.dao.MessageDao
import com.atride.cook.data.dao.SessionDao
import com.atride.cook.data.entity.MessageEntity
import com.atride.cook.data.entity.SessionEntity
import com.atride.cook.data.entity.SessionPreviewTuple
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.MessageRole
import com.atride.cook.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class ChatLocalDataSourceImpl(
    private val messageDao: MessageDao,
    private val sessionDao: SessionDao
) : ChatLocalDataSource {

    override fun getMessagesFlow(sessionId: String): Flow<List<ChatMessage>> {
        return messageDao.getBySessionFlow(sessionId).map { entities ->
            entities.map { it.toChatMessage() } // 🌟 名字非常直白
        }
    }

    override suspend fun getMessages(sessionId: String): List<ChatMessage> {
        return messageDao.getBySession(sessionId).map { it.toChatMessage() }
    }

    override suspend fun saveMessage(sessionId: String, message: ChatMessage) {
        val nextSortOrder = messageDao.maxSortOrder(sessionId) + 1

        messageDao.insertOrUpdate(message.toMessageEntity(sessionId, nextSortOrder))

        sessionDao.updateLastActiveTime(sessionId)
    }

    override suspend fun updateMessageContent(messageId: String, content: String, reasoning: String?) {
        val existing = messageDao.getById(messageId) ?: return

        messageDao.insertOrUpdate(
            existing.copy(
                content = content,
                reasoningContent = reasoning,
                status = "GENERATING"
            )
        )
    }

    override suspend fun ensureSessionExists(sessionId: String, defaultTitle: String) {
        if (sessionDao.getById(sessionId) == null) {
            val now = Clock.System.now().toEpochMilliseconds()
            sessionDao.upsertSession(
                SessionEntity(
                    id = sessionId,
                    title = defaultTitle,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    override fun getSessionsFlow(): Flow<List<Session>> {
        return sessionDao.getSessionPreviewsFlow().map { tuples ->
            tuples.map { it.toSession() }
        }
    }

    override suspend fun updateSessionTitle(sessionId: String, title: String) {
        sessionDao.updateTitle(sessionId, title)
    }

    override suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteById(sessionId)
    }

    override suspend fun clearMessages(sessionId: String) {
        messageDao.deleteBySession(sessionId)
    }
}

private fun MessageEntity.toChatMessage() = ChatMessage(
    id = this.id,
    content = this.content,
    reasoningContent = this.reasoningContent,
    role = when (this.role.lowercase()) {
        "user" -> MessageRole.USER
        "system" -> MessageRole.SYSTEM
        else -> MessageRole.ASSISTANT
    },
    isGenerating = this.status == "GENERATING",
    timestamp = this.createdAt
)

private fun ChatMessage.toMessageEntity(sessionId: String, sortOrder: Int) = MessageEntity(
    id = this.id,
    sessionId = sessionId,
    role = when (this.role) {
        MessageRole.USER -> "user"
        MessageRole.SYSTEM -> "system"
        MessageRole.ASSISTANT -> "assistant"
    },
    content = this.content,
    reasoningContent = this.reasoningContent,
    createdAt = this.timestamp,
    sortOrder = sortOrder,
    model = null,
    status = if (this.isGenerating) "GENERATING" else "SUCCESS"
)

private fun SessionPreviewTuple.toSession() = Session(
    id = this.id,
    title = this.title,
    lastMessage = this.lastMessage ?: "暂无消息",
    timestamp = this.updatedAt
)