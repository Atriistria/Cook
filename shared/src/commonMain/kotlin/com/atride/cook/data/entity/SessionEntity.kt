package com.atride.cook.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class SessionPreviewTuple(
    val id: String,
    val title: String,
    val updatedAt: Long,
    val lastMessage: String?,
)