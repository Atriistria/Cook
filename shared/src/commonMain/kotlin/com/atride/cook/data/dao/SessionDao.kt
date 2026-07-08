package com.atride.cook.data.dao

import androidx.room3.Dao
import androidx.room3.Query
import com.atride.cook.data.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

}