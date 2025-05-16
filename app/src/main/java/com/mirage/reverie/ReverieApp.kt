package com.mirage.reverie

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.hilt.android.HiltAndroidApp

// I don't know what's the use of application but it's needed
@HiltAndroidApp
class ReverieApp : Application() {
    companion object {
        lateinit var instance: ReverieApp
            private set
        lateinit var auth: FirebaseAuth
            private set
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth
        instance = this
    }
}