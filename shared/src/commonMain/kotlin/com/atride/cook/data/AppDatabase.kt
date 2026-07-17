package com.atride.cook.data

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.atride.cook.data.dao.SessionDao
import com.atride.cook.data.dao.MessageDao
import com.atride.cook.data.entity.SessionEntity
import com.atride.cook.data.entity.MessageEntity


@Database(
    entities = [SessionEntity::class, MessageEntity::class],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
