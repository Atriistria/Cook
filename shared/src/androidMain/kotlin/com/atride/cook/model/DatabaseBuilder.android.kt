package com.atride.cook.model

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.atride.cook.data.AppDatabase
import org.koin.mp.KoinPlatform

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    // 借助 Koin 动态获取我们在自定义 Application 中启动的 context 实例
    val context = KoinPlatform.getKoin().get<Context>()
    val dbFile = context.getDatabasePath("app.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}