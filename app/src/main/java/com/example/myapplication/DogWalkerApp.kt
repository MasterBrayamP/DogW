package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AppRepository
import com.example.myapplication.data.PrefsStore
import com.example.myapplication.data.SessionManager

class DogWalkerApp : Application() {
    lateinit var session: SessionManager
        private set
    lateinit var repo: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        session = SessionManager(this)
        repo = AppRepository(PrefsStore(this))
        repo.init()
    }

    companion object {
        lateinit var instance: DogWalkerApp
            private set
    }
}
