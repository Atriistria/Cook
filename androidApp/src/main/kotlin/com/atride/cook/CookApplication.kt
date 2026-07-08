package com.atride.cook

import android.app.Application
import com.atride.cook.di.initKoin

class CookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}