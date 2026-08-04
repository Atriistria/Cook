package com.atride.cook.data.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import com.atride.cook.data.entity.SessionEntity
import com.atride.cook.data.entity.SessionPreviewTuple
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
    fun getAllSession(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): SessionEntity?

    @Upsert
    suspend fun upsertSession(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("UPDATE sessions SET updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateLastActiveTime(
        sessionId: String,
        updatedAt: Long = Clock.System.now().toEpochMilliseconds()
    )

    @Query("UPDATE sessions SET title = :title WHERE id = :sessionId")
    suspend fun updateTitle(sessionId: String, title: String)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        """
        SELECT 
            s.id, 
            s.title, 
            s.updatedAt,
            (SELECT content FROM messages WHERE sessionId = s.id ORDER BY sortOrder DESC LIMIT 1) AS lastMessage
        FROM sessions s
        ORDER BY s.updatedAt DESC
    """
    )
    fun getSessionPreviewsFlow(): Flow<List<SessionPreviewTuple>>
}
