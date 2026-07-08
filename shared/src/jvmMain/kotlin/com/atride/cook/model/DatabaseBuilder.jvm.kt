package com.atride.cook.model

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.atride.cook.data.AppDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".cook/app.db")
    if (!dbFile.parentFile.exists()) {
        dbFile.parentFile.mkdirs()
    }
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}