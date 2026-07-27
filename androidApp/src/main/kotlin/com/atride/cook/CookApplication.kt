package com.atride.cook

import android.app.Application
import com.atride.cook.di.initKoin
import org.koin.android.ext.koin.androidContext

class CookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CookApplication)
        }
    }
}
