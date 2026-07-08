package com.atride.cook.model

import androidx.room3.RoomDatabase
import com.atride.cook.data.AppDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
