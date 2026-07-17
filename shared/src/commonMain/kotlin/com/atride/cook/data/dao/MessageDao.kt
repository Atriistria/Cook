package com.atride.cook.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.atride.cook.data.entity.MessageEntity

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY sortOrder ASC")
    suspend fun getBySession(sessionId: String): List<MessageEntity>

    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM messages WHERE sessionId = :sessionId")
    suspend fun maxSortOrder(sessionId: String): Int
}
