package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AppRepository
import com.example.myapplication.data.SessionManager
import com.example.myapplication.sqllite.DbDogWalker

class DogWalkerApp : Application() {
    lateinit var session: SessionManager   // SharedPreferences
        private set
    lateinit var repo: AppRepository       // SQLite (DbDogWalker)
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        session = SessionManager(this)
        repo = AppRepository(DbDogWalker(this))
        repo.init()
    }

    companion object {
        lateinit var instance: DogWalkerApp
            private set
    }
}
