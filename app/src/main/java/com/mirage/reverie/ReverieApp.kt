package com.mirage.reverie

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// I don't know what's the use of application but it's needed
@HiltAndroidApp
class ReverieApp : Application() {
    companion object {
        lateinit var instance: ReverieApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}