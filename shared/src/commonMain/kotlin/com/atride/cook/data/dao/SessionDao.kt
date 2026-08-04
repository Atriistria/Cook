package com.atride.cook.data.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import com.atride.cook.data.entity.SessionEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllSession(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): SessionEntity?

    @Upsert
    suspend fun upsertSession(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("UPDATE sessions SET updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateLastActiveTime(sessionId: String, updatedAt: Long = Clock.System.now().toEpochMilliseconds())

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}
